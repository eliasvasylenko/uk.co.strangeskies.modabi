/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.core.api.
 *
 * uk.co.strangeskies.modabi.core.api is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.core.api is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.core.api.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi;

import static uk.co.strangeskies.modabi.io.ModabiIOException.MESSAGES;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Path;

import uk.co.strangeskies.function.ThrowingSupplier;
import uk.co.strangeskies.modabi.io.ModabiIOException;
import uk.co.strangeskies.modabi.io.structured.StructuredDataWriter;
import uk.co.strangeskies.modabi.processing.BindingFuture;
import uk.co.strangeskies.modabi.schema.BindingPoint;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.reflection.token.TypeToken;

public interface OutputBinder<T> {
  <U> OutputBinder<U> from(BindingPoint<U> bindingPoint);

  <U> OutputBinder<U> from(Model<U> model);

  <U> OutputBinder<? super U> from(TypeToken<U> type);

  default <U> OutputBinder<? super U> from(Class<U> type) {
    return from(TypeToken.forClass(type));
  }

  OutputBinder<T> from(QualifiedName modelName);

  <U> OutputBinder<U> from(QualifiedName modelName, TypeToken<U> type);

  default <U> OutputBinder<U> from(QualifiedName modelName, Class<U> type) {
    return from(modelName, TypeToken.forClass(type));
  }

  BindingFuture<? super T> to(StructuredDataWriter output);

  // BindingFuture<T> to(RewritableStructuredData output);

  BindingFuture<? super T> to(Path output);

  default BindingFuture<? super T> to(URI output) {
    try {
      return to(output.toURL());
    } catch (MalformedURLException e) {
      throw new ModabiIOException(MESSAGES.invalidLocation(output), e);
    }
  }

  BindingFuture<? super T> to(URL output);

  BindingFuture<? super T> to(String extension, ThrowingSupplier<WritableByteChannel, ?> output);

  // Tnbinder<T> updatable();

  OutputBinder<T> withProvider(Provider provider);

  OutputBinder<T> withClassLoader(ClassLoader classLoader);
}
