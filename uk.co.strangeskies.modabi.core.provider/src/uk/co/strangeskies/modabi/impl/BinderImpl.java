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
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import uk.co.strangeskies.modabi.Binder;
import uk.co.strangeskies.modabi.Provider;
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
import uk.co.strangeskies.utilities.classpath.ManifestUtilities;
import uk.co.strangeskies.utilities.collection.ObservableSet.Change;
import uk.co.strangeskies.utilities.function.ThrowingSupplier;

public class BinderImpl<T> implements Binder<T> {
	private static final QualifiedName FORMAT_BLOCK_NAMESPACE = new QualifiedName("structuredDataFormat",
			Schema.MODABI_NAMESPACE);

	private final SchemaManagerImpl manager;
	private final Function<StructuredDataSource, Model<T>> bindingModelFunction;
	private final Consumer<BindingFuture<?>> addFuture;

	private final BindingBlocksImpl blocks;
	private ClassLoader classLoader;

	public BinderImpl(SchemaManagerImpl manager, Function<StructuredDataSource, Model<T>> bindingModelFunction,
			Consumer<BindingFuture<?>> addFuture) {
		this.manager = manager;
		this.bindingModelFunction = bindingModelFunction;
		this.addFuture = addFuture;

		blocks = new BindingBlocksImpl();
	}

	@Override
	public BindingFuture<T> from(URL input) {
		String extension = ManifestUtilities.getResourceExtension(input.getPath());

		if (extension != null) {
			return fromExtension(extension, input::openStream);
		} else {
			return from(input::openStream);
		}
	}

	@Override
	public BindingFuture<T> from(StructuredDataSource input) {
		return add(new BindingFutureImpl<>(manager, blocks, () -> {
			return new BindingSource<>(bindingModelFunction.apply(input).effective(), input);
		}, classLoader));
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
						Model.Effective<T> model = bindingModelFunction.apply(source).effective();

						return new BindingSource<>(model, input, format);
					} catch (Exception e) {
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
		Set<StructuredDataFormat> registeredFormats = new HashSet<>();

		Consumer<Change<StructuredDataFormat>> observer = change -> {
			synchronized (registeredFormats) {
				for (StructuredDataFormat format : change.added()) {
					if (!registeredFormats.contains(format)) {
						queue.accept(format);
					}
				}
			}
		};

		synchronized (registeredFormats) {
			manager.registeredFormats().changes().addWeakObserver(observer);
			registeredFormats.addAll(manager.registeredFormats());
		}

		try {
			BindingSource<T> source = getBindingSource.apply(registeredFormats);

			if (source == null) {
				BindingBlock block = blocks.block(FORMAT_BLOCK_NAMESPACE, Primitive.STRING, formatId, false);

				try {
					source = getBindingSource.apply(queue);
					block.complete();
				} catch (Exception e) {
					block.fail(e);
				}
			}

			return source;
		} catch (Exception e) {
			if (exception.get() == null)
				exception.set(e);

			throw new SchemaException("Could not bind input with any registered file loaders", exception.get());
		}
	}

	@Override
	public Binder<T> withProvider(Provider provider) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Binder<T> withRoot(T root) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Binder<T> withErrorHandler(Consumer<Exception> errorHandler) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Binder<T> withClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
		return this;
	}
}
