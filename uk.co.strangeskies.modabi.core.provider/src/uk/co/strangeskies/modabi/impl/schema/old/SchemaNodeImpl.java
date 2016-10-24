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
package uk.co.strangeskies.modabi.impl.schema.old;

import java.util.List;
import java.util.Objects;

import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.impl.schema.utilities.SchemaNodeConfigurationContext;
import uk.co.strangeskies.modabi.schema.BindingPoint;
import uk.co.strangeskies.modabi.schema.ChildBindingPoint;
import uk.co.strangeskies.modabi.schema.SchemaNode;
import uk.co.strangeskies.modabi.schema.SchemaNodeConfigurator;

public class SchemaNodeImpl implements SchemaNode {
	private final SchemaNodeConfigurator configurator;
	private final Schema schema;
	private final BindingPoint<?> bindingPoint;

	private final List<SchemaNode> baseNodes;

	private final List<ChildBindingPoint<?>> children;

	protected SchemaNodeImpl(SchemaNodeConfigurationContext context) {
		schema = context.schema();
		bindingPoint = context.bindingPoint();

		baseNodes = context.overriddenAndBaseNodes();

		SchemaNodeConfiguratorImpl configurator = context.configure(getThis());
		this.configurator = configurator;

		children = configurator.getChildrenResults();
	}

	@Override
	public List<SchemaNode> baseNodes() {
		return baseNodes;
	}

	@Override
	public List<ChildBindingPoint<?>> childBindingPoints() {
		return children;
	}

	@Override
	public BindingPoint<?> parentBindingPoint() {
		return bindingPoint;
	}

	@Override
	public boolean equals(Object that) {
		return that instanceof SchemaNode && Objects.equals(parentBindingPoint(), ((SchemaNode) that).parentBindingPoint());
	}

	@Override
	public int hashCode() {
		return ~Objects.hash(parentBindingPoint());
	}

	@Override
	public SchemaNodeConfigurator configurator() {
		return configurator.copy();
	}

	@Override
	public Schema schema() {
		return schema;
	}
}
