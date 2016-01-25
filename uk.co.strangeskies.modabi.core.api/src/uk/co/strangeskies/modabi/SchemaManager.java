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
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import uk.co.strangeskies.modabi.io.structured.DiscardingStructuredDataTarget;
import uk.co.strangeskies.modabi.io.structured.RewritableStructuredData;
import uk.co.strangeskies.modabi.io.structured.StructuredDataFormat;
import uk.co.strangeskies.modabi.io.structured.StructuredDataSource;
import uk.co.strangeskies.modabi.io.structured.StructuredDataTarget;
import uk.co.strangeskies.modabi.processing.BindingFuture;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.reflection.Reified;
import uk.co.strangeskies.reflection.TypeToken;

public interface SchemaManager {
	interface Binder<T> {
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

		BindingFuture<T> from(InputStream input);

		BindingFuture<T> from(String extension, InputStream input);

		// Binder<T> updatable();

		/*
		 * Errors which are rethrown will be passed to the next error handler if
		 * present, or dealt with as normal. Otherwise, a best effort is made at
		 * binding.
		 */
		Binder<T> with(Consumer<Exception> errorHandler);
	}

	interface Unbinder<T> {
		<U extends StructuredDataTarget> U to(U output);

		// BindingFuture<T> to(RewritableStructuredData output);

		default BindingFuture<T> to(File output) {
			return to(output.toURI());
		}

		default BindingFuture<T> to(URI output) {
			try {
				return to(output.toURL());
			} catch (MalformedURLException e) {
				throw new IllegalArgumentException(e);
			}
		}

		default BindingFuture<T> to(URL output) {
			String extension = output.getQuery();
			int lastDot = extension.lastIndexOf('.');
			if (lastDot > 0) {
				extension = extension.substring(lastDot);
			} else {
				throw new IllegalArgumentException(
						"No recognisable extension for file'" + output + "', data interface cannot be selected");
			}

			try (OutputStream fileStream = output.openConnection().getOutputStream()) {
				BindingFuture<T> binding = to(extension, fileStream);
				fileStream.flush();
				return binding;
			} catch (IOException e) {
				throw new IllegalArgumentException(e);
			}
		}

		BindingFuture<T> to(String extension, OutputStream output);

		// Unbinder<T> updatable();

		/*
		 * Errors which are rethrown will be passed to the next error handler if
		 * present, or dealt with as normal. Otherwise, a best effort is made at
		 * unbinding, and the exception information will be serialised as a comment.
		 */
		Unbinder<T> with(Consumer<Exception> errorHandler);
	}

	void registerDataInterface(StructuredDataFormat handler);

	void unregisterDataInterface(StructuredDataFormat handler);

	Set<StructuredDataFormat> getRegisteredDataInterfaces();

	StructuredDataFormat getDataInterface(String id);

	Set<StructuredDataFormat> getDataInterfaces(String extension);

	default GeneratedSchema generateSchema(QualifiedName name) {
		return generateSchema(name, Collections.emptySet());
	}

	default GeneratedSchema generateSchema(QualifiedName name, Schema... dependencies) {
		return generateSchema(name, Arrays.asList(dependencies));
	}

	GeneratedSchema generateSchema(QualifiedName name, Collection<? extends Schema> dependencies);

	<T> void registerProvider(TypeToken<T> providedClass, Supplier<T> provider);

	void registerProvider(Function<TypeToken<?>, ?> provider);

	default <T> void registerProvider(Class<T> providedClass, Supplier<T> provider) {
		registerProvider(TypeToken.over(providedClass), provider);
	}

	boolean registerSchema(Schema schema);

	<T> BindingFuture<T> registerBinding(Model<T> model, T data);

	// Blocks until all possible processing is done other than waiting imports:
	<T> Binder<T> bind(Model<T> model);

	// Blocks until all possible processing is done other than waiting imports:
	<T> Binder<T> bind(TypeToken<T> dataClass);

	default <T> Binder<T> bind(Class<T> dataClass) {
		return bind(TypeToken.over(dataClass));
	}

	Binder<?> bind();

	<T> Set<BindingFuture<T>> bindingFutures(Model<T> model);

	Binder<Schema> bindSchema();

	<T> Unbinder<T> unbind(Model<T> model, T data);

	<T> Unbinder<T> unbind(TypeToken<T> dataClass, T data);

	default <T> Unbinder<T> unbind(Class<T> dataClass, T data) {
		return unbind(TypeToken.over(dataClass), data);
	}

	default <T extends Reified<T>> Unbinder<T> unbind(T data) {
		return unbind(data.getThisType(), data);
	}

	<T> Unbinder<T> unbind(T data);

	MetaSchema getMetaSchema();

	BaseSchema getBaseSchema();

	Provisions provisions();

	Schemata registeredSchemata();

	Models registeredModels();

	DataTypes registeredTypes();
}
