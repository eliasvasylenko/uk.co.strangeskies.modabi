/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.core.provider.
 *
 * uk.co.strangeskies.modabi.core.provider is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.core.provider is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.core.provider.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi.impl.schema.utilities;

import java.util.List;

import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.impl.schema.old.SchemaNodeConfiguratorImpl;
import uk.co.strangeskies.modabi.schema.ChildBindingPoint;
import uk.co.strangeskies.modabi.schema.DataLoader;
import uk.co.strangeskies.modabi.schema.SchemaNode;
import uk.co.strangeskies.modabi.schema.SchemaNodeConfigurator;
import uk.co.strangeskies.reflection.BoundSet;
import uk.co.strangeskies.reflection.Imports;

public interface SchemaNodeConfigurationContext {
	ChildBindingPoint<?> bindingPoint();

	DataLoader dataLoader();

	Imports imports();

	BoundSet boundSet();

	List<SchemaNode> overriddenAndBaseNodes();

	SchemaNodeConfigurator configurator();

	Schema schema();

	/**
	 * Invoked by a {@link SchemaNode node's} constructor when instantiation
	 * begins. This allows a reference of the node being constructed to leak back
	 * to the caller whilst also blocking continuation of construction until
	 * configuration is complete and a configurator can be supplied.
	 * 
	 * @param node
	 *          the node being constructed
	 */
	SchemaNodeConfiguratorImpl configure(SchemaNode node);
}
