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

import static java.nio.channels.Channels.newChannel;
import static uk.co.strangeskies.modabi.binding.impl.BindingPointFuture.bindingPointFuture;
import static uk.co.strangeskies.modabi.binding.impl.BindingPointFuture.inputBindingPointFuture;
import static uk.co.strangeskies.modabi.binding.impl.InputBindingFuture.readBindingFuture;
import static uk.co.strangeskies.modabi.binding.impl.StructuredDataFuture.forData;
import static uk.co.strangeskies.modabi.binding.impl.StructuredDataFuture.forDataReader;

import java.net.URL;
import java.nio.channels.ReadableByteChannel;
import java.util.function.Predicate;

import uk.co.strangeskies.function.ThrowingSupplier;
import uk.co.strangeskies.modabi.DataFormats;
import uk.co.strangeskies.modabi.InputBinder;
import uk.co.strangeskies.modabi.Provider;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.binding.BindingFuture;
import uk.co.strangeskies.modabi.io.structured.DataFormat;
import uk.co.strangeskies.modabi.io.structured.StructuredDataReader;
import uk.co.strangeskies.modabi.schema.BindingPoint;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.reflection.token.TypeToken;

public class InputBinderImpl<T> implements InputBinder<T> {
  private final BindingContextImpl context;
  private final DataFormats formats;

  private final BindingPointFuture<T> bindingPointFuture;
  private final StructuredDataFuture<StructuredDataReader> dataReaderFuture;

  private final ClassLoader classLoader;

  protected InputBinderImpl(
      BindingContextImpl context,
      DataFormats formats,
      BindingPointFuture<T> bindingPointFuture,
      StructuredDataFuture<StructuredDataReader> dataReaderFuture,
      ClassLoader classLoader) {
    this.context = context;
    this.formats = formats;
    this.bindingPointFuture = bindingPointFuture;
    this.dataReaderFuture = dataReaderFuture;
    this.classLoader = classLoader;
  }

  public static InputBinder<?> bind(
      BindingContextImpl context,
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

  protected InputBinderImpl<T> with(StructuredDataFuture<StructuredDataReader> dataReaderFuture) {
    return new InputBinderImpl<>(
        context,
        formats,
        bindingPointFuture,
        dataReaderFuture,
        classLoader);
  }

  protected InputBinderImpl<T> with(
      String formatId,
      ThrowingSupplier<ReadableByteChannel, ?> input,
      Predicate<DataFormat> formatPredicate,
      boolean canRetry) {
    return new InputBinderImpl<>(
        context,
        formats,
        bindingPointFuture,
        forDataReader(context, formats, formatId, input, formatPredicate, canRetry),
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
  public <U> InputBinder<? extends U> to(TypeToken<U> type) {
    return with(inputBindingPointFuture(context, type));

  }

  @Override
  public InputBinder<?> to(QualifiedName name) {
    return with(bindingPointFuture(context, name));
  }

  @Override
  public <U> InputBinder<U> to(QualifiedName name, TypeToken<U> type) {
    return with(inputBindingPointFuture(context, name, type));
  }

  @Override
  public BindingFuture<? extends T> from(URL input) {
    return fromResource(input.getQuery(), () -> newChannel(input.openStream()));
  }

  public BindingFuture<? extends T> fromResource(
      String resourceName,
      ThrowingSupplier<ReadableByteChannel, ?> output) {
    int lastDot = resourceName.lastIndexOf(".");

    if (lastDot == -1) {
      return from(output);
    }

    String extension = resourceName.substring(0, lastDot);
    return from(extension, output);
  }

  @Override
  public BindingFuture<? extends T> from(StructuredDataReader dataReader) {
    return with(forData(dataReader)).getBindingFuture();
  }

  @Override
  public BindingFuture<? extends T> from(ThrowingSupplier<ReadableByteChannel, ?> input) {
    return with(null, input, f -> true, true).getBindingFuture();
  }

  @Override
  public BindingFuture<? extends T> from(
      String extension,
      ThrowingSupplier<ReadableByteChannel, ?> input) {
    return with(extension, input, f -> f.getFileExtensions().anyMatch(extension::equals), false)
        .getBindingFuture();
  }

  private BindingFuture<? extends T> getBindingFuture() {
    return readBindingFuture(context, bindingPointFuture, dataReaderFuture, classLoader);
  }
}
