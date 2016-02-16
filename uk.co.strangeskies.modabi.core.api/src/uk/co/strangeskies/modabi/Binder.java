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
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.function.Consumer;

import uk.co.strangeskies.modabi.io.structured.StructuredDataSource;
import uk.co.strangeskies.modabi.processing.BindingFuture;
import uk.co.strangeskies.utilities.function.ThrowingSupplier;

public interface Binder<T> {
	BindingFuture<T> from(StructuredDataSource input);

	// BindingFuture<T> from(RewritableStructuredData input);

	default BindingFuture<T> from(File input) {
		return from(input.toURI());
	}

	default BindingFuture<T> from(URI input) {
		try {
			return from(input.toURL());
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(e);
		}
	}

	BindingFuture<T> from(URL input);

	BindingFuture<T> from(ThrowingSupplier<InputStream, ?> input);

	BindingFuture<T> from(String extension, ThrowingSupplier<InputStream, ?> input);

	// Binder<T> updatable();

	Binder<T> with(ClassLoader classLoader);

	/*
	 * Errors which are rethrown will be passed to the next error handler if
	 * present, or dealt with as normal. Otherwise, a best effort is made at
	 * binding.
	 */
	Binder<T> with(Consumer<Exception> errorHandler);
}
