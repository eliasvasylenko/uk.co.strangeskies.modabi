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

import java.io.File;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.function.Consumer;

import uk.co.strangeskies.modabi.io.structured.StructuredDataTarget;
import uk.co.strangeskies.modabi.processing.BindingFuture;
import uk.co.strangeskies.utilities.function.ThrowingSupplier;

public interface Unbinder<T> {
	<U extends StructuredDataTarget> U to(U output);

	// BindingFuture<T> to(RewritableStructuredData output);

	BindingFuture<T> to(File output);

	default BindingFuture<T> to(URI output) {
		try {
			return to(output.toURL());
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(e);
		}
	}

	BindingFuture<T> to(URL output);

	BindingFuture<T> to(String extension, ThrowingSupplier<OutputStream, ?> output);

	// Unbinder<T> updatable();

	Unbinder<T> withProvider(Provider provider);

	Unbinder<T> withClassLoader(ClassLoader classLoader);

	/*
	 * Errors which are rethrown will be passed to the next error handler if
	 * present, or dealt with as normal. Otherwise, a best effort is made at
	 * unbinding, and the exception information will be serialised as a comment.
	 */
	Unbinder<T> withErrorHandler(Consumer<Exception> errorHandler);

}
