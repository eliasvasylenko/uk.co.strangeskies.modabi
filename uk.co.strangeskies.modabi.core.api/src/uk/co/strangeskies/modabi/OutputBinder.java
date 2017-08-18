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
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.function.Consumer;

import uk.co.strangeskies.function.ThrowingSupplier;
import uk.co.strangeskies.modabi.io.ModabiIOException;
import uk.co.strangeskies.modabi.io.structured.StructuredDataWriter;
import uk.co.strangeskies.modabi.processing.BindingFuture;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.reflection.token.TypeToken;

public interface OutputBinder<T> {
  OutputBinder<T> from(Model<? super T> model);

  OutputBinder<T> from(TypeToken<? super T> type);

  default OutputBinder<T> from(Class<? super T> type) {
    return from(TypeToken.forClass(type));
  }

  OutputBinder<T> from(QualifiedName modelName);

  OutputBinder<T> from(QualifiedName modelName, TypeToken<? super T> type);

  default OutputBinder<T> from(QualifiedName modelName, Class<? super T> type) {
    return from(modelName, TypeToken.forClass(type));
  }

  <U extends StructuredDataWriter> U to(U output);

  // BindingFuture<T> to(RewritableStructuredData output);

  BindingFuture<T> to(File output);

  default BindingFuture<T> to(URI output) {
    try {
      return to(output.toURL());
    } catch (MalformedURLException e) {
      throw new ModabiIOException(MESSAGES.invalidLocation(output), e);
    }
  }

  BindingFuture<T> to(URL output);

  BindingFuture<T> to(String extension, ThrowingSupplier<OutputStream, ?> output);

  // Tnbinder<T> updatable();

  OutputBinder<T> withProvider(Provider provider);

  OutputBinder<T> withClassLoader(ClassLoader classLoader);

  /*
   * Errors which are rethrown will be passed to the next error handler if
   * present, or dealt with as normal. Otherwise, a best effort is made at
   * unbinding, and the exception information will be serialised as a comment.
   */
  OutputBinder<T> withErrorHandler(Consumer<Exception> errorHandler);

}
