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
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import uk.co.strangeskies.modabi.Binder;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.impl.processing.BindingBlocksImpl;
import uk.co.strangeskies.modabi.impl.processing.BindingFutureImpl;
import uk.co.strangeskies.modabi.impl.processing.BindingFutureImpl.BindingSource;
import uk.co.strangeskies.modabi.io.Primitive;
import uk.co.strangeskies.modabi.io.structured.StructuredDataFormat;
import uk.co.strangeskies.modabi.io.structured.StructuredDataSource;
import uk.co.strangeskies.modabi.processing.BindingBlock;
import uk.co.strangeskies.modabi.processing.BindingFuture;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.utilities.ConsumerSupplierQueue;
import uk.co.strangeskies.utilities.IdentityProperty;
import uk.co.strangeskies.utilities.Property;
import uk.co.strangeskies.utilities.function.ThrowingSupplier;

public class BinderImpl<T> implements Binder<T> {
	private static final QualifiedName FORMAT_BLOCK_NAMESPACE = new QualifiedName("structuredDataFormat",
			Schema.MODABI_NAMESPACE);

	private final SchemaManagerImpl manager;
	private final Function<StructuredDataSource, Model<T>> bindingFunction;
	private final Consumer<BindingFuture<?>> addFuture;
	private final BindingBlocksImpl blocks;
	private ClassLoader classLoader;

	public BinderImpl(SchemaManagerImpl manager, Function<StructuredDataSource, Model<T>> bindingFunction,
			Consumer<BindingFuture<?>> addFuture) {
		this.manager = manager;
		this.bindingFunction = bindingFunction;
		this.addFuture = addFuture;
		blocks = new BindingBlocksImpl();
	}

	@Override
	public BindingFuture<T> from(URL input) {
		String extension = getExtension(input.getPath());

		if (extension != null) {
			return fromExtension(extension, input::openStream);
		} else {
			return from(input::openStream);
		}
	}

	@Override
	public BindingFuture<T> from(StructuredDataSource input) {
		return add(new BindingFutureImpl<>(manager, blocks, () -> {
			return new BindingSource<>(bindingFunction.apply(input).effective(), input);
		} , classLoader));
	}

	@Override
	public BindingFuture<T> from(ThrowingSupplier<InputStream, ?> input) {
		return add(
				new BindingFutureImpl<>(manager, blocks, () -> getBindingSource(null, input, f -> true, true), classLoader));
	}

	@Override
	public BindingFuture<T> from(String formatId, ThrowingSupplier<InputStream, ?> input) {
		return add(new BindingFutureImpl<>(manager, blocks,
				() -> getBindingSource(formatId, input, f -> f.getFormatId().equals(formatId), false), classLoader));
	}

	private BindingFuture<T> fromExtension(String extension, ThrowingSupplier<InputStream, ?> input) {
		return add(new BindingFutureImpl<>(manager, blocks,
				() -> getBindingSource(extension, input, f -> f.getFileExtensions().contains(extension), true), classLoader));
	}

	private BindingFuture<T> add(BindingFuture<T> bindingFuture) {
		addFuture.accept(bindingFuture);
		return bindingFuture;
	}

	private BindingSource<T> getBindingSource(String formatId, ThrowingSupplier<InputStream, ?> input,
			Predicate<StructuredDataFormat> formatPredicate, boolean canRetry) {
		Property<Exception, Exception> exception = new IdentityProperty<>();

		Function<Iterable<StructuredDataFormat>, BindingSource<T>> getBindingSource = formats -> {
			for (StructuredDataFormat format : formats) {
				if (formatPredicate.test(format)) {
					try (InputStream inputStream = input.get()) {
						StructuredDataSource source = format.loadData(inputStream);
						Model.Effective<T> model = bindingFunction.apply(source).effective();

						return new BindingSource<>(model, input, format);
					} catch (Exception e) {
						e.printStackTrace();
						exception.set(e);

						if (!canRetry) {
							throw new SchemaException(
									"Could not bind input with any file loader registered" + (formatId == null ? "" : " for " + formatId),
									exception.get());
						}
					}
				}
			}

			return null;
		};

		ConsumerSupplierQueue<StructuredDataFormat> queue = new ConsumerSupplierQueue<>();
		Set<StructuredDataFormat> registeredFormats = manager.dataFormats().registerObserver(queue);

		try {
			BindingSource<T> source = getBindingSource.apply(registeredFormats);

			if (source == null) {
				BindingBlock block = blocks.block(FORMAT_BLOCK_NAMESPACE, Primitive.STRING, formatId, false);
				new Thread(() -> {
					try {
						getBindingSource.apply(queue);
						block.complete();
					} catch (Exception e) {
						block.fail(e);
					}
				}).start();
				block.waitUntilComplete();
			}

			return source;
		} catch (Exception e) {
			if (exception.get() == null)
				exception.set(e);

			throw new SchemaException("Could not bind input with any registered file loaders", exception.get());
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

	static String getExtension(String name) {
		int lastSlash = name.lastIndexOf('/');
		if (lastSlash > 0) {
			name = name.substring(lastSlash);
		}

		int lastDot = name.lastIndexOf('.');
		if (lastDot > 0) {
			name = name.substring(lastDot + 1);
		} else {
			name = null;
		}

		return name;
	}
}
