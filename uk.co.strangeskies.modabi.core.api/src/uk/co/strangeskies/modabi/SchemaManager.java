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
import uk.co.strangeskies.modabi.schema.DataLoader;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.reflection.token.ReifiedToken;
import uk.co.strangeskies.utilities.Scoped;
import uk.co.strangeskies.utilities.collection.ObservableSet;

public interface SchemaManager extends Scoped<SchemaManager> {
	BaseSchema getBaseSchema();

	MetaSchema getMetaSchema();

	/**
	 * @return a schema builder who's products will be automatically registered
	 *         with this manager
	 */
	SchemaBuilder getSchemaBuilder();

	/**
	 * Create a {@link SchemaConfigurator} equivalent to one created via
	 * {@link #getSchemaBuilder()} with a default {@link DataLoader} provided.
	 * 
	 * @return a schema configurator who's products will be automatically
	 *         registered with this manager
	 */
	SchemaConfigurator getSchemaConfigurator();

	<T> ObservableSet<?, BindingFuture<T>> getBindingFutures(Model<T> model);

	<T> ObservableSet<?, Binding<T>> getBindings(Model<T> model);

	Provisions provisions();

	Schemata registeredSchemata();

	Models registeredModels();

	DataFormats registeredFormats();

	InputBinder<?> bindInput();

	default InputBinder<Schema> bindSchema() {
		return bindInput().with(getMetaSchema().getSchemaModel());
	}

	<T> OutputBinder<T> bindOutput(T data);

	default <T extends ReifiedToken<T>> OutputBinder<T> bindOutput(T data) {
		return bindOutput(data).with(data.getThisTypeToken());
	}
}
