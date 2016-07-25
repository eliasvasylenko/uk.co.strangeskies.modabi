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
package uk.co.strangeskies.modabi.schema;

import java.util.function.Function;

import uk.co.strangeskies.modabi.Abstractness;
import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.schema.building.ChildBuilder;
import uk.co.strangeskies.utilities.Factory;
import uk.co.strangeskies.utilities.Self;

public interface SchemaNodeConfigurator<S extends SchemaNodeConfigurator<S, N>, N extends SchemaNode<N>>
		extends Factory<N>, Self<S> {
	public S name(QualifiedName name);

	public default S name(String name, Namespace namespace) {
		return name(new QualifiedName(name, namespace));
	}

	S abstractness(Abstractness abstractness);

	public ChildBuilder addChild();

	public default SchemaNodeConfigurator<?, N> addChild(Function<ChildBuilder, SchemaNodeConfigurator<?, ?>> builder) {
		builder.apply(addChild()).create();

		return this;
	}
}
