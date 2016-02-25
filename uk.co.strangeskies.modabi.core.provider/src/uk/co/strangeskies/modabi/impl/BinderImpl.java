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
package uk.co.strangeskies.modabi.impl;

import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import uk.co.strangeskies.modabi.Binder;
import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.impl.processing.BindingFutureImpl;
import uk.co.strangeskies.modabi.impl.processing.BindingFutureImpl.BindingSource;
import uk.co.strangeskies.modabi.io.structured.StructuredDataFormat;
import uk.co.strangeskies.modabi.io.structured.StructuredDataSource;
import uk.co.strangeskies.modabi.processing.BindingFuture;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.utilities.ConsumerSupplierQueue;
import uk.co.strangeskies.utilities.function.ThrowingSupplier;

public class BinderImpl<T> implements Binder<T> {
	private final SchemaManagerImpl manager;
	private final Function<StructuredDataSource, Model<T>> bindingFunction;
	private final Consumer<BindingFuture<?>> addFuture;
	private ClassLoader classLoader;

	public BinderImpl(SchemaManagerImpl manager, Function<StructuredDataSource, Model<T>> bindingFunction,
			Consumer<BindingFuture<?>> addFuture) {
		this.manager = manager;
		this.bindingFunction = bindingFunction;
		this.addFuture = addFuture;
	}

	@Override
	public BindingFuture<T> from(URL input) {
		String extension = input.getPath();
		int lastSlash = extension.lastIndexOf('/');
		if (lastSlash > 0) {
			extension = extension.substring(lastSlash);

			int lastDot = extension.lastIndexOf('.');
			if (lastDot > 0) {
				extension = extension.substring(lastDot + 1);
			} else {
				extension = null;
			}
		} else {
			extension = null;
		}

		if (extension != null) {
			return fromExtension(extension, input::openStream);
		} else {
			return from(input::openStream);
		}
	}

	@Override
	public BindingFuture<T> from(StructuredDataSource input) {
		return add(new BindingFutureImpl<>(manager, () -> {
			return new BindingSource<>(bindingFunction.apply(input).effective(), input);
		} , classLoader));
	}

	@Override
	public BindingFuture<T> from(ThrowingSupplier<InputStream, ?> input) {
		return add(new BindingFutureImpl<>(manager, () -> getBindingSource(input, f -> true, true), classLoader));
	}

	@Override
	public BindingFuture<T> from(String formatId, ThrowingSupplier<InputStream, ?> input) {
		return add(new BindingFutureImpl<>(manager,
				() -> getBindingSource(input, f -> f.getFormatId().equals(formatId), false), classLoader));
	}

	private BindingFuture<T> fromExtension(String extension, ThrowingSupplier<InputStream, ?> input) {
		return add(new BindingFutureImpl<>(manager,
				() -> getBindingSource(input, f -> f.getFileExtensions().contains(extension), true), classLoader));
	}

	private BindingFuture<T> add(BindingFuture<T> bindingFuture) {
		addFuture.accept(bindingFuture);
		return bindingFuture;
	}

	private BindingSource<T> getBindingSource(ThrowingSupplier<InputStream, ?> input,
			Predicate<StructuredDataFormat> formatPredicate, boolean canRetry) {
		Exception exception = null;

		ConsumerSupplierQueue<StructuredDataFormat> queue = new ConsumerSupplierQueue<>();
		Iterator<StructuredDataFormat> formatIterator = manager.dataFormats().registerObserver(queue).iterator();

		try {
			while (true) {
				StructuredDataFormat format;
				if (formatIterator.hasNext()) {
					format = formatIterator.next();
					formatIterator.remove();
				} else {
					// TODO set waiting for format flag
					format = queue.get();
				}

				if (formatPredicate.test(format)) {
					try (InputStream inputStream = input.get()) {
						StructuredDataSource source = format.loadData(inputStream);
						Model.Effective<T> model = bindingFunction.apply(source).effective();

						return new BindingSource<>(model, input, format);
					} catch (Exception e) {
						e.printStackTrace();
						exception = e;

						if (!canRetry) {
							// TODO
							throw new SchemaException("Could not bind input with file loader registered for ${ID}" + null, exception);
						}
					}
				}
			}
		} catch (Exception e) {
			if (exception == null)
				exception = e;

			throw new SchemaException("Could not bind input with any registered file loaders", exception);
		}
	}

	@Override
	public Binder<T> with(Consumer<Exception> errorHandler) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Binder<T> with(ClassLoader classLoader) {
		this.classLoader = classLoader;
		return this;
	}
}
