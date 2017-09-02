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
package uk.co.strangeskies.modabi.impl.processing;

import static uk.co.strangeskies.modabi.impl.processing.BindingPointFuture.bindingPointFuture;
import static uk.co.strangeskies.modabi.impl.processing.InputBindingFuture.readBindingFuture;

import java.io.InputStream;
import java.net.URL;
import java.util.function.Consumer;
import java.util.function.Predicate;

import uk.co.strangeskies.function.ThrowingSupplier;
import uk.co.strangeskies.modabi.DataFormats;
import uk.co.strangeskies.modabi.InputBinder;
import uk.co.strangeskies.modabi.Provider;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.io.structured.DataFormat;
import uk.co.strangeskies.modabi.io.structured.StructuredDataReader;
import uk.co.strangeskies.modabi.processing.BindingFuture;
import uk.co.strangeskies.modabi.schema.BindingPoint;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.reflection.token.TypeToken;

public class InputBinderImpl<T> implements InputBinder<T> {
  private final ProcessingContextImpl context;
  private final DataFormats formats;

  private final BindingPointFuture<T> bindingPointFuture;
  private final DataReaderFuture dataReaderFuture;

  private final ClassLoader classLoader;

  protected InputBinderImpl(
      ProcessingContextImpl context,
      DataFormats formats,
      BindingPointFuture<T> bindingPointFuture,
      DataReaderFuture dataReaderFuture,
      ClassLoader classLoader) {
    this.context = context;
    this.formats = formats;
    this.bindingPointFuture = bindingPointFuture;
    this.dataReaderFuture = dataReaderFuture;
    this.classLoader = classLoader;
  }

  public static InputBinder<?> bind(
      ProcessingContextImpl context,
      DataFormats formats,
      Model<Object> rootModel) {
    return new InputBinderImpl<>(
        context,
        formats,
        bindingPointFuture(context, rootModel),
        null,
        Thread.currentThread().getContextClassLoader());
  }

  @Override
  public InputBinder<T> withProvider(Provider provider) {
    return new InputBinderImpl<>(
        context.withProvider(provider),
        formats,
        bindingPointFuture,
        dataReaderFuture,
        classLoader);
  }

  @Override
  public InputBinder<T> withErrorHandler(Consumer<Exception> errorHandler) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public InputBinderImpl<T> withClassLoader(ClassLoader classLoader) {
    return new InputBinderImpl<>(
        context,
        formats,
        bindingPointFuture,
        dataReaderFuture,
        classLoader);
  }

  protected <U> InputBinderImpl<U> with(BindingPointFuture<U> bindingPointFuture) {
    return new InputBinderImpl<>(
        context,
        formats,
        bindingPointFuture,
        dataReaderFuture,
        classLoader);
  }

  protected InputBinderImpl<T> with(DataReaderFuture dataReaderFuture) {
    return new InputBinderImpl<>(
        context,
        formats,
        bindingPointFuture,
        dataReaderFuture,
        classLoader);
  }

  protected InputBinderImpl<T> with(
      String formatId,
      ThrowingSupplier<InputStream, ?> input,
      Predicate<DataFormat> formatPredicate,
      boolean canRetry) {
    return new InputBinderImpl<>(
        context,
        formats,
        bindingPointFuture,
        new DataReaderFuture(context, formats, formatId, input, formatPredicate, canRetry),
        classLoader);
  }

  @Override
  public <U> InputBinder<U> to(BindingPoint<U> bindingPoint) {
    return with(bindingPointFuture(context, bindingPoint));
  }

  @Override
  public <U> InputBinder<U> to(Model<U> model) {
    return with(bindingPointFuture(context, model));
  }

  @Override
  public <U> InputBinder<U> to(TypeToken<U> type) {
    return with(bindingPointFuture(context, type));

  }

  @Override
  public InputBinder<?> to(QualifiedName name) {
    return with(bindingPointFuture(context, name));
  }

  @Override
  public <U> InputBinder<U> to(QualifiedName name, TypeToken<U> type) {
    return with(bindingPointFuture(context, name, type));
  }

  @Override
  public BindingFuture<? extends T> from(URL input) {
    String extension = input.getPath().substring(0, input.getPath().lastIndexOf("."));

    if (extension != null) {
      return fromExtension(extension, input::openStream);
    } else {
      return from(input::openStream);
    }
  }

  @Override
  public BindingFuture<? extends T> from(StructuredDataReader dataReader) {
    return with(new DataReaderFuture(dataReader)).getBindingFuture();
  }

  @Override
  public BindingFuture<? extends T> from(ThrowingSupplier<InputStream, ?> input) {
    return with(null, input, f -> true, true).getBindingFuture();
  }

  @Override
  public BindingFuture<? extends T> from(String formatId, ThrowingSupplier<InputStream, ?> input) {
    return with(formatId, input, f -> f.getFormatId().equals(formatId), false).getBindingFuture();
  }

  private BindingFuture<? extends T> fromExtension(
      String extension,
      ThrowingSupplier<InputStream, ?> input) {
    return with(extension, input, f -> f.getFileExtensions().contains(extension), true)
        .getBindingFuture();
  }

  private BindingFuture<? extends T> getBindingFuture() {
    return readBindingFuture(context, bindingPointFuture, dataReaderFuture, classLoader);
  }
}
