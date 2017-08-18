/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.core.provider.
 *
 * uk.co.strangeskies.modabi.core.provider is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.core.provider is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.core.provider.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi.impl;

import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static uk.co.strangeskies.modabi.processing.ProcessingException.MESSAGES;
import static uk.co.strangeskies.observable.Observable.merge;

import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import uk.co.strangeskies.function.ThrowingSupplier;
import uk.co.strangeskies.modabi.DataFormats;
import uk.co.strangeskies.modabi.InputBinder;
import uk.co.strangeskies.modabi.Provider;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.impl.processing.BindingFutureImpl;
import uk.co.strangeskies.modabi.impl.processing.BindingFutureImpl.BindingSource;
import uk.co.strangeskies.modabi.impl.processing.ProcessingContextImpl;
import uk.co.strangeskies.modabi.io.Primitive;
import uk.co.strangeskies.modabi.io.structured.StructuredDataFormat;
import uk.co.strangeskies.modabi.io.structured.StructuredDataReader;
import uk.co.strangeskies.modabi.processing.BindingBlock;
import uk.co.strangeskies.modabi.processing.BindingFuture;
import uk.co.strangeskies.modabi.processing.ProcessingException;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.observable.Observable;
import uk.co.strangeskies.reflection.token.TypeToken;

public class InputBinderImpl<T> implements InputBinder<T> {
  private static final QualifiedName FORMAT_BLOCK_NAMESPACE = new QualifiedName(
      "structuredDataFormat",
      Schema.MODABI_NAMESPACE);

  private final ProcessingContextImpl context;
  private final DataFormats formats;

  private final Function<StructuredDataReader, Model<T>> bindingModelFunction;

  private ClassLoader classLoader;

  protected InputBinderImpl(
      ProcessingContextImpl context,
      DataFormats formats,
      Function<StructuredDataReader, Model<T>> bindingModelFunction) {
    this.context = context;
    this.formats = formats;

    this.bindingModelFunction = bindingModelFunction;
  }

  public static InputBinder<?> bind(ProcessingContextImpl context, DataFormats formats) {
    return new InputBinderImpl<>(context, formats, data -> {
      try {
        return context.registeredModels().waitForGet(data.peekNextChild());
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    });
  }

  protected <U> InputBinder<U> with(Function<StructuredDataReader, Model<U>> bindingModelFunction) {
    return new InputBinderImpl<>(context, formats, bindingModelFunction);
  }

  @Override
  public <U> InputBinder<U> to(Model<U> model) {
    return with(data -> {
      if (!context.registeredModels().contains(model)) {
        throw new ProcessingException(
            MESSAGES.noModelFound(model.name(), context.registeredModels(), model.dataType()),
            context);
      }

      if (!data.peekNextChild().equals(model.name())) {
        throw new ProcessingException(MESSAGES.unexpectedElement(data.peekNextChild()), context);
      }

      return model;
    });
  }

  @Override
  public <U> InputBinder<U> to(TypeToken<U> type) {
    return with(data -> {
      try {
        return context.registeredModels().waitForGet(data.peekNextChild(), type);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    });
  }

  @Override
  public InputBinder<?> to(QualifiedName name) {
    return with(data -> {
      try {
        return context.registeredModels().waitForGet(name);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    });
  }

  @Override
  public <U> InputBinder<U> to(QualifiedName name, TypeToken<U> type) {
    return with(data -> {
      try {
        return context.registeredModels().waitForGet(name, type);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    });
  }

  @Override
  public BindingFuture<T> from(URL input) {
    String extension = input.getPath().substring(0, input.getPath().lastIndexOf("."));

    if (extension != null) {
      return fromExtension(extension, input::openStream);
    } else {
      return from(input::openStream);
    }
  }

  @Override
  public BindingFuture<T> from(StructuredDataReader input) {
    return createBindingFuture(() -> new BindingSource<>(bindingModelFunction.apply(input), input));
  }

  @Override
  public BindingFuture<T> from(ThrowingSupplier<InputStream, ?> input) {
    return createBindingFuture(() -> getBindingSource(null, input, f -> true, true));
  }

  @Override
  public BindingFuture<T> from(String formatId, ThrowingSupplier<InputStream, ?> input) {
    return createBindingFuture(
        () -> getBindingSource(formatId, input, f -> f.getFormatId().equals(formatId), false));
  }

  private BindingFuture<T> fromExtension(String extension, ThrowingSupplier<InputStream, ?> input) {
    return createBindingFuture(
        () -> getBindingSource(
            extension,
            input,
            f -> f.getFileExtensions().contains(extension),
            true));
  }

  private BindingFuture<T> createBindingFuture(Supplier<BindingSource<T>> modelSupplier) {
    ClassLoader classLoader = this.classLoader != null
        ? this.classLoader
        : Thread.currentThread().getContextClassLoader();
    BindingFuture<T> bindingFuture = new BindingFutureImpl<>(context, classLoader, modelSupplier);

    return bindingFuture;
  }

  private BindingSource<T> getBindingSource(
      String formatId,
      ThrowingSupplier<InputStream, ?> input,
      Predicate<StructuredDataFormat> formatPredicate,
      boolean canRetry) {
    Set<Exception> exceptions = new HashSet<>();

    Function<StructuredDataFormat, BindingSource<T>> getBindingSource = format -> {
      if (formatPredicate.test(format)) {
        try (InputStream inputStream = input.get()) {
          StructuredDataReader source = format.loadData(inputStream);
          Model<T> model = bindingModelFunction.apply(source);

          return new BindingSource<T>(model, input, format);
        } catch (Exception e) {
          if (canRetry) {
            exceptions.add(e);
          } else {
            throw e;
          }
        }
      }

      return null;
    };

    CompletableFuture<BindingSource<T>> sourceFuture;

    synchronized (formats) {
      sourceFuture = merge(
          Observable.of(formats),
          formats.changes().flatMap(change -> Observable.of(change.added())))
              .executeOn(newSingleThreadExecutor())
              .map(getBindingSource::apply)
              .getNext();
    }

    try {
      if (sourceFuture.isCompletedExceptionally() || sourceFuture.isDone()) {
        return sourceFuture.get();
      } else {
        BindingBlock block = context
            .bindingBlocker()
            .block(FORMAT_BLOCK_NAMESPACE, Primitive.STRING, formatId, false);

        try {
          BindingSource<T> source = sourceFuture.get();
          block.complete();
          return source;
        } catch (Exception e) {
          block.fail(e);
          throw e;
        }
      }
    } catch (Exception e) {
      for (Exception exception : exceptions)
        e.addSuppressed(exception);

      throw new ProcessingException(
          formatId == null ? MESSAGES.noFormatFound() : MESSAGES.noFormatFoundFor(formatId),
          context,
          e);
    }
  }

  @Override
  public InputBinder<T> withProvider(Provider provider) {
    context.provisions().add(provider);
    return this;
  }

  @Override
  public InputBinder<T> withErrorHandler(Consumer<Exception> errorHandler) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public InputBinder<T> withClassLoader(ClassLoader classLoader) {
    this.classLoader = classLoader;
    return this;
  }
}
