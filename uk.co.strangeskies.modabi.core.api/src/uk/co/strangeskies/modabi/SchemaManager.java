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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Supplier;

import uk.co.strangeskies.modabi.io.structured.StructuredDataSource;
import uk.co.strangeskies.modabi.io.structured.StructuredDataTarget;
import uk.co.strangeskies.modabi.processing.BindingFuture;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.reflection.TypeToken;

public interface SchemaManager {
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

	default <T> T bind(Model<T> model, StructuredDataSource input) {
		return bindFuture(model, input).resolveNow().getData();
	}

	default <T> T bind(TypeToken<T> dataClass, StructuredDataSource input) {
		return bindFuture(dataClass, input).resolveNow().getData();
	}

	default <T> T bind(Class<T> dataClass, StructuredDataSource input) {
		return bindFuture(dataClass, input).resolveNow().getData();
	}

	default Binding<?> bind(StructuredDataSource input) {
		return bindFuture(input).resolveNow();
	}

	// Blocks until all possible processing is done other than waiting imports:
	<T> BindingFuture<T> bindFuture(Model<T> model, StructuredDataSource input);

	// Blocks until all possible processing is done other than waiting imports:
	<T> BindingFuture<T> bindFuture(TypeToken<T> dataClass,
			StructuredDataSource input);

	default <T> BindingFuture<T> bindFuture(Class<T> dataClass,
			StructuredDataSource input) {
		return bindFuture(TypeToken.over(dataClass), input);
	}

	BindingFuture<?> bindFuture(StructuredDataSource input);

	<T> Set<BindingFuture<T>> bindingFutures(Model<T> model);

	default Schema registerSchemaBinding(StructuredDataSource input) {
		Schema schema = bind(getMetaSchema().getSchemaModel(), input);

		registerSchema(schema);

		return schema;
	}

	default BindingFuture<Schema> registerSchemaBindingFuture(
			StructuredDataSource input) {
		BindingFuture<Schema> schema = bindFuture(getMetaSchema().getSchemaModel(),
				input);

		new Thread(() -> {
			try {
				registerSchema(schema.get().getData());
			} catch (InterruptedException | ExecutionException
					| CancellationException e) {}
		});

		return schema;
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
