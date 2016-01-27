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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.concurrent.FutureTask;
import java.util.function.Consumer;
import java.util.function.Function;

import uk.co.strangeskies.modabi.Binder;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.impl.processing.BindingContextImpl;
import uk.co.strangeskies.modabi.impl.processing.BindingNodeBinder;
import uk.co.strangeskies.modabi.io.structured.StructuredDataFormat;
import uk.co.strangeskies.modabi.io.structured.StructuredDataSource;
import uk.co.strangeskies.modabi.processing.BindingException;
import uk.co.strangeskies.modabi.processing.BindingFuture;
import uk.co.strangeskies.modabi.schema.Model;

public class BinderImpl<T> implements Binder<T> {
	private final SchemaManagerImpl manager;
	private final Function<StructuredDataSource, Model<T>> bindingFunction;

	public BinderImpl(SchemaManagerImpl manager, Function<StructuredDataSource, Model<T>> bindingFunction) {
		this.manager = manager;
		this.bindingFunction = bindingFunction;
	}

	@Override
	public BindingFuture<T> from(StructuredDataSource input) {
		BindingFuture<T> bindingFuture = bind(bindingFunction.apply(input).effective(), input);
		manager.addBindingFuture(bindingFuture);
		return bindingFuture;
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

		try (InputStream fileStream = input.openStream()) {
			if (extension != null) {
				return from(extension, fileStream);
			} else {
				return from(fileStream);
			}
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public BindingFuture<T> from(InputStream input) {
		return from(input, manager.dataInterfaces().getRegisteredDataInterfaces());
	}

	@Override
	public BindingFuture<T> from(String extension, InputStream input) {
		return from(input, manager.dataInterfaces().getDataInterfaces(extension));
	}

	private BindingFuture<T> from(InputStream input, Collection<? extends StructuredDataFormat> loaders) {
		BufferedInputStream bufferedInput = new BufferedInputStream(input);
		bufferedInput.mark(4096);

		if (loaders.isEmpty())
			throw new IllegalArgumentException("No valid file loader registered for input");

		Exception exception = null;

		for (StructuredDataFormat loader : loaders) {
			try {
				return from(loader.loadData(bufferedInput));
			} catch (Exception e) {
				exception = e;
			}
			try {
				bufferedInput.reset();
			} catch (IOException e) {
				throw new IllegalArgumentException("Problem buffering input for binding", e);
			}
		}

		throw new IllegalArgumentException("Could not bind input with any registered file loaders", exception);
	}

	@Override
	public Binder<T> with(Consumer<Exception> errorHandler) {
		// TODO Auto-generated method stub
		return null;
	}

	private BindingFuture<T> bind(Model.Effective<T> model, StructuredDataSource input) {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

		BindingContextImpl context = manager.getBindingContext().withInput(input);

		QualifiedName inputRoot = input.startNextChild();
		if (!inputRoot.equals(model.getName()))
			throw new BindingException("Model '" + model.getName() + "' does not match root input node '" + inputRoot + "'",
					context);

		FutureTask<T> future = new FutureTask<>(() -> {
			Thread.currentThread().setContextClassLoader(classLoader);

			try {
				return new BindingNodeBinder(context).bind(model);
			} catch (SchemaException e) {
				throw e;
			} catch (Exception e) {
				throw new BindingException("Unexpected problem during binding", context, e);
			}
		});
		future.run();

		return BindingFuture.forFuture(model, future);
	}
}
