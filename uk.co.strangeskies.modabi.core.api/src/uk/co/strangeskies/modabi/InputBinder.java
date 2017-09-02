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

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.function.Consumer;

import uk.co.strangeskies.function.ThrowingSupplier;
import uk.co.strangeskies.modabi.io.ModabiIOException;
import uk.co.strangeskies.modabi.io.structured.StructuredDataReader;
import uk.co.strangeskies.modabi.processing.BindingFuture;
import uk.co.strangeskies.modabi.schema.BindingPoint;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.reflection.token.TypeToken;

public interface InputBinder<T> {
  <U> InputBinder<U> to(BindingPoint<U> bindingPoint);

  <U> InputBinder<U> to(Model<U> model);

  <U> InputBinder<U> to(TypeToken<U> type);

  default <U> InputBinder<U> to(Class<U> type) {
    return to(TypeToken.forClass(type));
  }

  InputBinder<?> to(QualifiedName modelName);

  <U> InputBinder<U> to(QualifiedName modelName, TypeToken<U> type);

  default <U> InputBinder<U> to(QualifiedName modelName, Class<U> type) {
    return to(modelName, TypeToken.forClass(type));
  }

  BindingFuture<? extends T> from(StructuredDataReader input);

  // BindingFuture<T> from(RewritableStructuredData input);

  default BindingFuture<? extends T> from(File input) {
    return from(input.toURI());
  }

  default BindingFuture<? extends T> from(URI input) {
    try {
      return from(input.toURL());
    } catch (MalformedURLException e) {
      throw new ModabiIOException(MESSAGES.invalidLocation(input), e);
    }
  }

  BindingFuture<? extends T> from(URL input);

  BindingFuture<? extends T> from(ThrowingSupplier<InputStream, ?> input);

  BindingFuture<? extends T> from(String formatId, ThrowingSupplier<InputStream, ?> input);

  // Binder<T> updatable();

  InputBinder<T> withProvider(Provider provider);

  InputBinder<T> withClassLoader(ClassLoader classLoader);

  /*
   * Errors which are rethrown will be passed to the next error handler if
   * present, or dealt with as normal. Otherwise, a best effort is made at
   * binding.
   */
  InputBinder<T> withErrorHandler(Consumer<Exception> errorHandler);
}
