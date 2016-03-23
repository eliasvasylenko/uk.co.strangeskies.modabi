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

import uk.co.strangeskies.modabi.processing.BindingFuture;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.reflection.Reified;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.utilities.collection.ObservableSet;

public interface SchemaManager {
	BaseSchema getBaseSchema();

	MetaSchema getMetaSchema();

	SchemaConfigurator getSchemaConfigurator();

	<T> Binder<T> bind(Model<T> model);

	<T> Binder<T> bind(TypeToken<T> dataClass);

	default <T> Binder<T> bind(Class<T> dataClass) {
		return bind(TypeToken.over(dataClass));
	}

	Binder<?> bind();

	default Binder<Schema> bindSchema() {
		return bind(getMetaSchema().getSchemaModel());
	}

	<T> ObservableSet<?, BindingFuture<T>> getBindingFutures(Model<T> model);

	<T> ObservableSet<?, Binding<T>> getBindings(Model<T> model);

	<T> Unbinder<T> unbind(Model<T> model, T data);

	<T> Unbinder<T> unbind(TypeToken<T> dataClass, T data);

	default <T> Unbinder<T> unbind(Class<T> dataClass, T data) {
		return unbind(TypeToken.over(dataClass), data);
	}

	default <T extends Reified<T>> Unbinder<T> unbind(T data) {
		return unbind(data.getThisType(), data);
	}

	<T> Unbinder<T> unbind(T data);

	Provisions provisions();

	Schemata registeredSchemata();

	Models registeredModels();

	DataTypes registeredTypes();

	DataFormats dataFormats();
	
	SchemaManagerScope childScope();
}
