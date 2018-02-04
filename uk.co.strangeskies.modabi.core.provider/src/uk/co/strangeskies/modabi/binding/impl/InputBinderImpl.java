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

import static java.lang.Thread.currentThread;
import static java.nio.channels.Channels.newChannel;
import static java.nio.file.StandardOpenOption.READ;
import static uk.co.strangeskies.modabi.DataFormats.getExtension;
import static uk.co.strangeskies.modabi.Models.getBindingPoint;
import static uk.co.strangeskies.modabi.Models.getInputBindingPoint;
import static uk.co.strangeskies.modabi.io.ModabiIOException.MESSAGES;

import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;

import uk.co.strangeskies.function.ThrowingSupplier;
import uk.co.strangeskies.modabi.Binding;
import uk.co.strangeskies.modabi.DataFormats;
import uk.co.strangeskies.modabi.InputBinder;
import uk.co.strangeskies.modabi.Provider;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.io.ModabiIOException;
import uk.co.strangeskies.modabi.io.structured.DataFormat;
import uk.co.strangeskies.modabi.io.structured.StructuredDataReader;
import uk.co.strangeskies.modabi.schema.BindingPoint;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.reflection.token.TypeToken;

public class InputBinderImpl<T> implements InputBinder<T> {
  private final BindingContextImpl context;
  private final DataFormats formats;

  private final BindingPoint<T> bindingPoint;
  private final StructuredDataReader dataReader;

  private final ClassLoader classLoader;

  protected InputBinderImpl(
      BindingContextImpl context,
      DataFormats formats,
      BindingPoint<T> bindingPoint,
      StructuredDataReader dataReader,
      ClassLoader classLoader) {
    this.context = context;
    this.formats = formats;
    this.bindingPoint = bindingPoint;
    this.dataReader = dataReader;
    this.classLoader = classLoader;
  }

  public static InputBinder<?> bind(BindingContextImpl context, DataFormats formats) {
    return new InputBinderImpl<>(
        context,
        formats,
        getBindingPoint(context.manager().schemata().getBaseSchema().rootModel()),
        null,
        currentThread().getContextClassLoader());
  }

  @Override
  public InputBinder<T> withProvider(Provider provider) {
    return new InputBinderImpl<>(
        context.withProvider(provider),
        formats,
        bindingPoint,
        dataReader,
        classLoader);
  }

  @Override
  public InputBinderImpl<T> withClassLoader(ClassLoader classLoader) {
    return new InputBinderImpl<>(context, formats, bindingPoint, dataReader, classLoader);
  }

  protected <U> InputBinderImpl<U> with(BindingPoint<U> bindingPoint) {
    return new InputBinderImpl<>(context, formats, bindingPoint, dataReader, classLoader);
  }

  protected InputBinderImpl<T> with(StructuredDataReader dataReader) {
    return new InputBinderImpl<>(context, formats, bindingPoint, dataReader, classLoader);
  }

  protected InputBinderImpl<T> with(
      String formatId,
      ThrowingSupplier<ReadableByteChannel, ?> input) {
    DataFormat format = formats.getFormat(formatId);
    StructuredDataReader dataReader;
    try {
      dataReader = format.readData(input.get());
    } catch (Exception e) {
      throw new ModabiIOException(MESSAGES.cannotOpenResource(), e);
    }

    return new InputBinderImpl<>(context, formats, bindingPoint, dataReader, classLoader);
  }

  @Override
  public <U> InputBinder<U> to(BindingPoint<U> bindingPoint) {
    return with(bindingPoint);
  }

  @Override
  public <U> InputBinder<U> to(Model<U> model) {
    return with(getBindingPoint(model));
  }

  @Override
  public <U> InputBinder<? extends U> to(TypeToken<U> type) {
    return with(
        getInputBindingPoint(context.manager().schemata().getBaseSchema().rootModel(), type));

  }

  @Override
  public InputBinder<?> to(QualifiedName modelName) {
    return with(getBindingPoint(context.manager().schemata().models().get(modelName)));
  }

  @Override
  public <U> InputBinder<U> to(QualifiedName modelName, TypeToken<U> type) {
    return with(getInputBindingPoint(context.manager().schemata().models().get(modelName), type));
  }

  @Override
  public Binding<? extends T> from(Path input) {
    return from(getExtension(input.getFileName().toString()), () -> FileChannel.open(input, READ));
  }

  @Override
  public Binding<? extends T> from(URL input) {
    return from(getExtension(input.getQuery()), () -> newChannel(input.openStream()));
  }

  @Override
  public Binding<? extends T> from(StructuredDataReader dataReader) {
    return with(dataReader).getBinding();
  }

  @Override
  public Binding<? extends T> from(
      String extension,
      ThrowingSupplier<ReadableByteChannel, ?> input) {
    return with(extension, input).getBinding();
  }

  private Binding<? extends T> getBinding() {
    BindingContextImpl context = this.context.withInput(dataReader);
    return new NodeReader<T>().bind(context, bindingPoint);
  }
}
