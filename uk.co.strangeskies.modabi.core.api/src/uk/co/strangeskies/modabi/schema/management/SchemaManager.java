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
package uk.co.strangeskies.modabi.schema.management;

import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Supplier;

import uk.co.strangeskies.modabi.io.structured.StructuredDataSource;
import uk.co.strangeskies.modabi.io.structured.StructuredDataTarget;
import uk.co.strangeskies.modabi.schema.BaseSchema;
import uk.co.strangeskies.modabi.schema.Binding;
import uk.co.strangeskies.modabi.schema.MetaSchema;
import uk.co.strangeskies.modabi.schema.Schema;
import uk.co.strangeskies.modabi.schema.Schemata;
import uk.co.strangeskies.modabi.schema.management.binding.BindingFuture;
import uk.co.strangeskies.modabi.schema.node.Model;
import uk.co.strangeskies.reflection.TypeToken;

public interface SchemaManager {
	<T> void registerProvider(TypeToken<T> providedClass, Supplier<T> provider);

	void registerProvider(Function<TypeToken<?>, ?> provider);

	default <T> void registerProvider(Class<T> providedClass, Supplier<T> provider) {
		registerProvider(TypeToken.of(providedClass), provider);
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
		return bindFuture(TypeToken.of(dataClass), input);
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

	<T> void unbind(Model<T> model, StructuredDataTarget output, T data);

	<T> void unbind(TypeToken<T> dataClass, StructuredDataTarget output, T data);

	default <T> void unbind(Class<T> dataClass, StructuredDataTarget output,
			T data) {
		unbind(TypeToken.of(dataClass), output, data);
	}

	void unbind(StructuredDataTarget output, Object data);

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

	DataBindingTypes registeredTypes();
}
