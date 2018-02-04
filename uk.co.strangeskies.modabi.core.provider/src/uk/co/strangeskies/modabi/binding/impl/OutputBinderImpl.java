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
import static java.nio.file.StandardOpenOption.WRITE;
import static uk.co.strangeskies.modabi.DataFormats.getExtension;
import static uk.co.strangeskies.modabi.Models.getBindingPoint;
import static uk.co.strangeskies.modabi.Models.getOutputBindingPoint;
import static uk.co.strangeskies.modabi.io.ModabiIOException.MESSAGES;

import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Path;

import uk.co.strangeskies.function.ThrowingSupplier;
import uk.co.strangeskies.modabi.Binding;
import uk.co.strangeskies.modabi.DataFormats;
import uk.co.strangeskies.modabi.OutputBinder;
import uk.co.strangeskies.modabi.Provider;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.io.ModabiIOException;
import uk.co.strangeskies.modabi.io.structured.DataFormat;
import uk.co.strangeskies.modabi.io.structured.StructuredDataWriter;
import uk.co.strangeskies.modabi.schema.BindingPoint;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.reflection.token.TypeToken;

public class OutputBinderImpl<T> implements OutputBinder<T> {
  private final BindingContextImpl context;
  private final DataFormats formats;
  private final Object data;

  private final BindingPoint<T> bindingPoint;
  private final StructuredDataWriter dataWriter;

  private ClassLoader classLoader;

  protected OutputBinderImpl(
      BindingContextImpl context,
      DataFormats formats,
      Object data,
      BindingPoint<T> bindingPoint,
      StructuredDataWriter dataWriter,
      ClassLoader classLoader) {
    this.context = context;
    this.formats = formats;
    this.data = data;
    this.bindingPoint = bindingPoint;
    this.dataWriter = dataWriter;
    this.classLoader = classLoader;
  }

  public static <T> OutputBinder<? super T> bind(
      BindingContextImpl context,
      DataFormats formats,
      T data) {
    return new OutputBinderImpl<>(
        context,
        formats,
        data,
        getBindingPoint(context.manager().schemata().getBaseSchema().rootModel()),
        null,
        currentThread().getContextClassLoader());
  }

  @Override
  public OutputBinderImpl<T> withProvider(Provider provider) {
    return new OutputBinderImpl<>(
        context.withProvider(provider),
        formats,
        data,
        bindingPoint,
        dataWriter,
        classLoader);
  }

  @Override
  public OutputBinderImpl<T> withClassLoader(ClassLoader classLoader) {
    return new OutputBinderImpl<>(context, formats, data, bindingPoint, dataWriter, classLoader);
  }

  protected <U> OutputBinderImpl<U> with(BindingPoint<U> bindingPoint) {
    return new OutputBinderImpl<>(context, formats, data, bindingPoint, dataWriter, classLoader);
  }

  protected OutputBinderImpl<T> with(StructuredDataWriter dataWriter) {
    return new OutputBinderImpl<>(context, formats, data, bindingPoint, dataWriter, classLoader);
  }

  protected OutputBinderImpl<T> with(
      String formatId,
      ThrowingSupplier<WritableByteChannel, ?> output) {
    DataFormat format = formats.getFormat(formatId);
    StructuredDataWriter dataWriter;
    try {
      dataWriter = format.writeData(output.get());
    } catch (Exception e) {
      throw new ModabiIOException(MESSAGES.cannotOpenResource(), e);
    }

    return new OutputBinderImpl<T>(context, formats, data, bindingPoint, dataWriter, classLoader);
  }

  @Override
  public <U> OutputBinder<U> from(BindingPoint<U> bindingPoint) {
    return with(bindingPoint);
  }

  @Override
  public <U> OutputBinder<U> from(Model<U> model) {
    return with(getBindingPoint(model));
  }

  @Override
  public <U> OutputBinder<? super U> from(TypeToken<U> dataType) {
    return with(
        getOutputBindingPoint(context.manager().schemata().getBaseSchema().rootModel(), dataType));
  }

  @SuppressWarnings("unchecked")
  @Override
  public OutputBinder<T> from(QualifiedName modelName) {
    return from(modelName, (Class<T>) data.getClass());
  }

  @Override
  public <U> OutputBinder<U> from(QualifiedName modelName, TypeToken<U> type) {
    return with(getOutputBindingPoint(context.manager().schemata().models().get(modelName), type));
  }

  @Override
  public Binding<? extends T> to(Path output) {
    return to(getExtension(output.getFileName().toString()), () -> FileChannel.open(output, WRITE));
  }

  @Override
  public Binding<? extends T> to(URL output) {
    try {
      return to(
          getExtension(output.getQuery()),
          () -> Channels.newChannel(output.openConnection().getOutputStream()));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Binding<? extends T> to(
      String extension,
      ThrowingSupplier<WritableByteChannel, ?> output) {
    return with(extension, output).getBinding();
  }

  @Override
  public Binding<? extends T> to(StructuredDataWriter output) {
    return with(output).getBinding();
  }

  @SuppressWarnings("unchecked")
  private Binding<? extends T> getBinding() {
    BindingContextImpl context = this.context.withOutput(dataWriter);
    return new NodeWriter().bind(context, bindingPoint, (T) data);
  }
}
