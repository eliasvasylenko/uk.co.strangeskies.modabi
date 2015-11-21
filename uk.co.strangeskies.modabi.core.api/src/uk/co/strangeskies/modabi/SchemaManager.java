/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import uk.co.strangeskies.modabi.io.structured.DataInterface;
import uk.co.strangeskies.modabi.io.structured.StructuredDataSource;
import uk.co.strangeskies.modabi.io.structured.StructuredDataTarget;
import uk.co.strangeskies.modabi.processing.BindingFuture;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.reflection.Reified;
import uk.co.strangeskies.reflection.TypeToken;

public interface SchemaManager {
	interface Binder<T> {
		BindingFuture<T> from(StructuredDataSource input);

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

		default BindingFuture<T> from(URL input) {
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

		BindingFuture<T> from(InputStream input);

		BindingFuture<T> from(String extension, InputStream input);
	}

	interface Unbinder {
		/*
		 * TODO If errors are rethrown, deal with as normal. Otherwise. best effort
		 * at unbinding, outputting comments on errors instead of throwing
		 * exceptions.
		 */
		// Unbinder with(Consumer<Exception> errorHandler);

		<U extends StructuredDataTarget> U to(U output);

		default void to(File output) {
			to(output.toURI());
		}

		default void to(URI output) {
			try {
				to(output.toURL());
			} catch (MalformedURLException e) {
				throw new IllegalArgumentException(e);
			}
		}

		default void to(URL output) {
			String extension = output.getQuery();
			int lastDot = extension.lastIndexOf('.');
			if (lastDot > 0) {
				extension = extension.substring(lastDot);
			} else {
				throw new IllegalArgumentException("No recognisable extension for file'"
						+ output + "', data interface cannot be selected");
			}

			try (
					OutputStream fileStream = output.openConnection().getOutputStream()) {
				to(extension, fileStream).flush();
			} catch (IOException e) {
				throw new IllegalArgumentException(e);
			}
		}

		<U extends OutputStream> U to(String extension, U output);
	}

	void registerDataInterface(DataInterface handler);

	void unregisterDataInterface(DataInterface handler);

	Set<DataInterface> getRegisteredDataInterfaces();

	DataInterface getDataInterface(String id);

	Set<DataInterface> getDataInterfaces(String extension);

	default GeneratedSchema generateSchema(QualifiedName name) {
		return generateSchema(name, Collections.emptySet());
	}

	default GeneratedSchema generateSchema(QualifiedName name,
			Schema... dependencies) {
		return generateSchema(name, Arrays.asList(dependencies));
	}

	GeneratedSchema generateSchema(QualifiedName name,
			Collection<? extends Schema> dependencies);

	<T> void registerProvider(TypeToken<T> providedClass, Supplier<T> provider);

	void registerProvider(Function<TypeToken<?>, ?> provider);

	default <T> void registerProvider(Class<T> providedClass,
			Supplier<T> provider) {
		registerProvider(TypeToken.over(providedClass), provider);
	}

	void registerSchema(Schema schema);

	// So we can import from manually added data.
	void registerBinding(Binding<?> binding);

	default <T> void registerBinding(Model<T> model, T data) {
		registerBinding(new Binding<T>(model, data));
	}

	// Blocks until all possible processing is done other than waiting imports:
	<T> Binder<T> bind(Model<T> model);

	// Blocks until all possible processing is done other than waiting imports:
	<T> Binder<T> bind(TypeToken<T> dataClass);

	default <T> Binder<T> bind(Class<T> dataClass) {
		return bind(TypeToken.over(dataClass));
	}

	Binder<?> bind();

	<T> Set<BindingFuture<T>> bindingFutures(Model<T> model);

	default Binder<Schema> bindSchema() {
		Binder<Schema> binder = bind(getMetaSchema().getSchemaModel());

		return new Binder<Schema>() {
			@Override
			public BindingFuture<Schema> from(StructuredDataSource input) {
				return registerFuture(binder.from(input));
			}

			@Override
			public BindingFuture<Schema> from(URL input) {
				return registerFuture(binder.from(input));
			}

			@Override
			public BindingFuture<Schema> from(InputStream input) {
				return registerFuture(binder.from(input));
			}

			@Override
			public BindingFuture<Schema> from(String extension, InputStream input) {
				return registerFuture(binder.from(extension, input));
			}

			private BindingFuture<Schema> registerFuture(BindingFuture<Schema> from) {
				new Thread(() -> {
					registerSchema(from.resolve());
				}).start();

				return from;
			}
		};
	}

	<T> Unbinder unbind(Model<T> model, T data);

	<T> Unbinder unbind(TypeToken<T> dataClass, T data);

	default <T> Unbinder unbind(Class<T> dataClass, T data) {
		return unbind(TypeToken.over(dataClass), data);
	}

	default <T extends Reified<T>> Unbinder unbind(T data) {
		return unbind(data.getThisType(), data);
	}

	Unbinder unbind(Object data);

	MetaSchema getMetaSchema();

	BaseSchema getBaseSchema();

	Provisions provisions();

	Schemata registeredSchemata();

	Models registeredModels();

	DataTypes registeredTypes();
}
