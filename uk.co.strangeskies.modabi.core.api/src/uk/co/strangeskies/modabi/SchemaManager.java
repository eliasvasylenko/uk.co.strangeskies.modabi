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
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import uk.co.strangeskies.modabi.io.structured.FileLoader;
import uk.co.strangeskies.modabi.io.structured.StructuredDataSource;
import uk.co.strangeskies.modabi.io.structured.StructuredDataTarget;
import uk.co.strangeskies.modabi.processing.BindingFuture;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.reflection.TypeToken;

public interface SchemaManager {
	interface Binder<T> {
		BindingFuture<T> from(StructuredDataSource input);

		BindingFuture<T> from(File input);

		BindingFuture<T> from(InputStream input);

		BindingFuture<T> from(InputStream input, String extension);
	}

	void registerFileLoader(FileLoader loader);

	void unregisterFileLoader(FileLoader loader);

	Set<FileLoader> getRegisteredFileLoaders();

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
			public BindingFuture<Schema> from(File input) {
				return registerFuture(binder.from(input));
			}

			@Override
			public BindingFuture<Schema> from(InputStream input) {
				return registerFuture(binder.from(input));
			}

			@Override
			public BindingFuture<Schema> from(InputStream input, String extension) {
				return registerFuture(binder.from(input, extension));
			}

			private BindingFuture<Schema> registerFuture(BindingFuture<Schema> from) {
				new Thread(() -> {
					registerSchema(from.resolve());
				}).start();

				return from;
			}
		};
	}

	<T, U extends StructuredDataTarget> U unbind(Model<T> model, U output,
			T data);

	<T, U extends StructuredDataTarget> U unbind(TypeToken<T> dataClass, U output,
			T data);

	default <T, U extends StructuredDataTarget> U unbind(Class<T> dataClass,
			U output, T data) {
		return unbind(TypeToken.over(dataClass), output, data);
	}

	<U extends StructuredDataTarget> U unbind(U output, Object data);

	/*-
	 * TODO Best effort at unbinding, outputting comments on errors instead of
	 * throwing exceptions
	 *
	 * <T> Set<Exception> unbind(Model<T> model, StructuredDataTarget output, T
	 * data);
	 */
	MetaSchema getMetaSchema();

	BaseSchema getBaseSchema();

	Provisions provisions();

	Schemata registeredSchemata();

	Models registeredModels();

	DataTypes registeredTypes();
}
