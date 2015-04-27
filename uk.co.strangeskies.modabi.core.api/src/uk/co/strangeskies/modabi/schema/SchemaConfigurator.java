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
package uk.co.strangeskies.modabi.schema;

import java.util.Collection;

import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.node.DataBindingType;
import uk.co.strangeskies.modabi.schema.node.Model;
import uk.co.strangeskies.utilities.factory.Factory;

public interface SchemaConfigurator extends Factory<Schema> {
	public SchemaConfigurator qualifiedName(QualifiedName name);

	public SchemaConfigurator dependencies(
			Collection<? extends Schema> dependencies);

	public SchemaConfigurator types(Collection<? extends DataBindingType<?>> types);

	public SchemaConfigurator models(Collection<? extends Model<?>> models);
}
