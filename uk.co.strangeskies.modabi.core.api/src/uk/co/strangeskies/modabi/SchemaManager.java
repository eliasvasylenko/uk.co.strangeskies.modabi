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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import uk.co.strangeskies.modabi.processing.BindingFuture;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.reflection.Reified;
import uk.co.strangeskies.reflection.TypeToken;

public interface SchemaManager {
	DataFormats dataFormats();

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
