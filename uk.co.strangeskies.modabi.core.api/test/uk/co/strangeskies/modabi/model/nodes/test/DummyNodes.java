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
package uk.co.strangeskies.modabi.model.nodes.test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.schema.BindingCondition;
import uk.co.strangeskies.modabi.schema.BindingPoint;
import uk.co.strangeskies.modabi.schema.ChildBindingPoint;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.SchemaNode;
import uk.co.strangeskies.modabi.schema.SchemaNodeConfigurator;
import uk.co.strangeskies.reflection.TypeToken;

public class DummyNodes {
	private DummyNodes() {}

	public static SchemaNode schemaNode(String... children) {
		return schemaNode(Arrays.stream(children).map(DummyNodes::childBindingPoint).collect(Collectors.toList()));
	}

	public static SchemaNode schemaNode(QualifiedName... children) {
		return schemaNode(Arrays.stream(children).map(DummyNodes::childBindingPoint).collect(Collectors.toList()));
	}

	public static SchemaNode schemaNode(ChildBindingPoint<?>... children) {
		return schemaNode(Arrays.asList(children));
	}

	public static SchemaNode schemaNode(List<ChildBindingPoint<?>> children) {
		return new SchemaNode() {
			@Override
			public List<SchemaNode> baseNodes() {
				return Collections.emptyList();
			}

			@Override
			public Schema schema() {
				return null;
			}

			@Override
			public SchemaNodeConfigurator configurator() {
				return null;
			}

			@Override
			public BindingPoint<?> parentBindingPoint() {
				return null;
			}

			@Override
			public List<ChildBindingPoint<?>> childBindingPoints() {
				return children;
			}
		};
	}

	public static ChildBindingPoint<?> childBindingPoint(String name) {
		return childBindingPoint(new QualifiedName(name));
	}

	public static ChildBindingPoint<?> childBindingPoint(QualifiedName name) {
		return childBindingPoint(name, schemaNode(Collections.emptyList()));
	}

	public static ChildBindingPoint<?> childBindingPoint(String name, SchemaNode node) {
		return childBindingPoint(new QualifiedName(name), node);
	}

	public static ChildBindingPoint<?> childBindingPoint(QualifiedName name, SchemaNode node) {
		return new ChildBindingPoint<Object>() {
			@Override
			public QualifiedName name() {
				return name;
			}

			@Override
			public SchemaNode node() {
				return node;
			}

			@Override
			public TypeToken<Object> dataType() {
				return new TypeToken<Object>() {};
			}

			@Override
			public BindingCondition<Object> bindingCondition() {
				return BindingCondition.optional();
			}

			@Override
			public TypeToken<ChildBindingPoint<Object>> getThisType() {
				return new TypeToken<ChildBindingPoint<Object>>() {};
			}

			@Override
			public List<Object> providedValues() {
				return null;
			}

			@Override
			public boolean concrete() {
				return true;
			}

			@Override
			public List<Model<? super Object>> baseModel() {
				return Collections.emptyList();
			}

			@Override
			public boolean extensible() {
				return false;
			}

			@Override
			public TypeToken<?> preInputType() {
				return dataType();
			}

			@Override
			public TypeToken<?> postInputType() {
				return dataType();
			}
		};
	}
}
