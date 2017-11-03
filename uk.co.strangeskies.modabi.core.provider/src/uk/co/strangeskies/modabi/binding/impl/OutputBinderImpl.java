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
package uk.co.strangeskies.modabi.binding.impl;

import static java.nio.file.StandardOpenOption.WRITE;
import static uk.co.strangeskies.modabi.binding.BindingException.MESSAGES;
import static uk.co.strangeskies.modabi.binding.impl.BindingPointFuture.bindingPointFuture;
import static uk.co.strangeskies.modabi.binding.impl.BindingPointFuture.outputBindingPointFuture;
import static uk.co.strangeskies.modabi.binding.impl.OutputBindingFuture.writeBindingFuture;
import static uk.co.strangeskies.modabi.binding.impl.StructuredDataFuture.forData;
import static uk.co.strangeskies.modabi.binding.impl.StructuredDataFuture.forDataWriter;

import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Path;
import java.util.function.Predicate;

import uk.co.strangeskies.function.ThrowingSupplier;
import uk.co.strangeskies.modabi.DataFormats;
import uk.co.strangeskies.modabi.OutputBinder;
import uk.co.strangeskies.modabi.Provider;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.binding.BindingFuture;
import uk.co.strangeskies.modabi.binding.BindingException;
import uk.co.strangeskies.modabi.io.structured.DataFormat;
import uk.co.strangeskies.modabi.io.structured.StructuredDataWriter;
import uk.co.strangeskies.modabi.schema.BindingPoint;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.reflection.token.TypeToken;

public class OutputBinderImpl<T> implements OutputBinder<T> {
  private final BindingContextImpl context;
  private final DataFormats formats;
  private final Object data;

  private final BindingPointFuture<T> bindingPointFuture;
  private final StructuredDataFuture<StructuredDataWriter> dataWriterFuture;

  private ClassLoader classLoader;

  protected OutputBinderImpl(
      BindingContextImpl context,
      DataFormats formats,
      Object data,
      BindingPointFuture<T> bindingPointFuture,
      StructuredDataFuture<StructuredDataWriter> dataWriterFuture,
      ClassLoader classLoader) {
    this.context = context;
    this.formats = formats;
    this.data = data;
    this.bindingPointFuture = bindingPointFuture;
    this.dataWriterFuture = dataWriterFuture;
    this.classLoader = classLoader;
  }

  public static <T> OutputBinder<? super T> bind(
      BindingContextImpl context,
      DataFormats formats,
      Model<Object> rootModel,
      T data) {
    return new OutputBinderImpl<>(
        context,
        formats,
        data,
        bindingPointFuture(context, rootModel),
        null,
        Thread.currentThread().getContextClassLoader());
  }

  @Override
  public OutputBinderImpl<T> withProvider(Provider provider) {
    return new OutputBinderImpl<>(
        context.withProvider(provider),
        formats,
        data,
        bindingPointFuture,
        dataWriterFuture,
        classLoader);
  }

  @Override
  public OutputBinderImpl<T> withClassLoader(ClassLoader classLoader) {
    return new OutputBinderImpl<>(
        context,
        formats,
        data,
        bindingPointFuture,
        dataWriterFuture,
        classLoader);
  }

  protected <U> OutputBinderImpl<U> with(BindingPointFuture<U> bindingPointFuture) {
    return new OutputBinderImpl<>(
        context,
        formats,
        data,
        bindingPointFuture,
        dataWriterFuture,
        classLoader);
  }

  protected OutputBinderImpl<T> with(StructuredDataFuture<StructuredDataWriter> dataWriterFuture) {
    return new OutputBinderImpl<>(
        context,
        formats,
        data,
        bindingPointFuture,
        dataWriterFuture,
        classLoader);
  }

  protected OutputBinderImpl<T> with(
      String formatId,
      ThrowingSupplier<WritableByteChannel, ?> output,
      Predicate<DataFormat> formatPredicate,
      boolean canRetry) {
    return new OutputBinderImpl<T>(
        context,
        formats,
        data,
        bindingPointFuture,
        forDataWriter(context, formats, formatId, output, formatPredicate, canRetry),
        classLoader);
  }

  @Override
  public <U> OutputBinder<U> from(BindingPoint<U> bindingPoint) {
    return with(bindingPointFuture(context, bindingPoint));
  }

  @Override
  public <U> OutputBinder<U> from(Model<U> model) {
    return with(bindingPointFuture(context, model));
  }

  @Override
  public <U> OutputBinder<? super U> from(TypeToken<U> dataType) {
    return with(outputBindingPointFuture(context, dataType));
  }

  @SuppressWarnings("unchecked")
  @Override
  public OutputBinder<T> from(QualifiedName modelName) {
    return from(modelName, (Class<T>) data.getClass());
  }

  @Override
  public <U> OutputBinder<U> from(QualifiedName modelName, TypeToken<U> type) {
    return with(outputBindingPointFuture(context, modelName, type));
  }

  @Override
  public BindingFuture<? extends T> to(Path output) {
    return toResource(output.getFileName().toString(), () -> FileChannel.open(output, WRITE));
  }

  @Override
  public BindingFuture<? extends T> to(URL output) {
    try {
      return toResource(
          output.getQuery(),
          () -> Channels.newChannel(output.openConnection().getOutputStream()));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public BindingFuture<? extends T> toResource(
      String resourceName,
      ThrowingSupplier<WritableByteChannel, ?> output) {
    int lastDot = resourceName.lastIndexOf(".");

    if (lastDot == -1) {
      throw new BindingException(MESSAGES.noFormatFoundFor(resourceName), context);
    }

    String extension = resourceName.substring(0, lastDot);
    return to(extension, output);
  }

  @Override
  public BindingFuture<? extends T> to(
      String extension,
      ThrowingSupplier<WritableByteChannel, ?> output) {
    return with(extension, output, f -> f.getFileExtensions().anyMatch(extension::equals), false)
        .getBindingFuture();
  }

  @Override
  public BindingFuture<? extends T> to(StructuredDataWriter output) {
    return with(forData(output)).getBindingFuture();
  }

  private BindingFuture<? extends T> getBindingFuture() {
    return writeBindingFuture(context, bindingPointFuture, dataWriterFuture, classLoader, data);
  }
}
