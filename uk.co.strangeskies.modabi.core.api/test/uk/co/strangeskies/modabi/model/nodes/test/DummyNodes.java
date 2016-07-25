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

import uk.co.strangeskies.mathematics.Range;
import uk.co.strangeskies.modabi.Abstractness;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.schema.BindingNode;
import uk.co.strangeskies.modabi.schema.ChildNode;
import uk.co.strangeskies.modabi.schema.SchemaNode;
import uk.co.strangeskies.modabi.schema.SequenceNode;
import uk.co.strangeskies.modabi.schema.SequenceNodeConfigurator;
import uk.co.strangeskies.reflection.TypeToken;

public class DummyNodes {
	private DummyNodes() {}

	public static SequenceNode sequenceNode(String name, String... children) {
		return sequenceNode(new QualifiedName(name),
				Arrays.asList(children).stream().map(DummyNodes::sequenceNode).collect(Collectors.toList()));
	}

	public static SequenceNode sequenceNode(QualifiedName name, QualifiedName... children) {
		return sequenceNode(name,
				Arrays.asList(children).stream().map(DummyNodes::sequenceNode).collect(Collectors.toList()));
	}

	public static SequenceNode sequenceNode(String name, ChildNode<?>... children) {
		return sequenceNode(new QualifiedName(name), children);
	}

	public static SequenceNode sequenceNode(QualifiedName name, ChildNode<?>... children) {
		return sequenceNode(name, Arrays.asList(children));
	}

	public static SequenceNode sequenceNode(QualifiedName name, List<ChildNode<?>> children) {
		return new SequenceNode() {
			@Override
			public QualifiedName name() {
				return name;
			}

			@Override
			public Abstractness abstractness() {
				return Abstractness.CONCRETE;
			}

			@Override
			public List<ChildNode<?>> children() {
				return children;
			}

			@Override
			public boolean equals(Object object) {
				if (!(object instanceof SequenceNode))
					return false;

				if (object == this)
					return true;

				return ((SequenceNode) object).name().equals(name()) && children.equals(((SequenceNode) object).children());
			}

			@Override
			public int hashCode() {
				return name().hashCode() + children.hashCode();
			}

			@Override
			public TypeToken<?> postInputType() {
				return TypeToken.over(Object.class);
			}

			@Override
			public TypeToken<?> preInputType() {
				return TypeToken.over(Object.class);
			}

			@Override
			public Range<Integer> occurrences() {
				return Range.between(1, 1);
			}

			@Override
			public Boolean ordered() {
				return true;
			}

			@Override
			public SchemaNode<?> parent() {
				return null;
			}

			@Override
			public BindingNode<?, ?> root() {
				return null;
			}

			@Override
			public Schema schema() {
				return null;
			}

			@Override
			public SequenceNodeConfigurator configurator() {
				return null;
			}
		};
	}

	public static SequenceNode sequenceNode(String name) {
		return sequenceNode(new QualifiedName(name));
	}

	public static SequenceNode sequenceNode(QualifiedName name) {
		return new SequenceNode() {
			@Override
			public QualifiedName name() {
				return name;
			}

			@Override
			public List<ChildNode<?>> children() {
				return Collections.emptyList();
			}

			@Override
			public Abstractness abstractness() {
				return Abstractness.CONCRETE;
			}

			@Override
			public boolean equals(Object object) {
				if (!(object instanceof SequenceNode))
					return false;

				return ((SequenceNode) object).name().equals(name());
			}

			@Override
			public int hashCode() {
				return name().hashCode();
			}

			@Override
			public TypeToken<?> preInputType() {
				return TypeToken.over(Object.class);
			}

			@Override
			public TypeToken<?> postInputType() {
				return TypeToken.over(Object.class);
			}

			@Override
			public Range<Integer> occurrences() {
				return Range.between(1, 1);
			}

			@Override
			public Boolean ordered() {
				return true;
			}

			@Override
			public SchemaNode<?> parent() {
				return null;
			}

			@Override
			public BindingNode<?, ?> root() {
				return null;
			}

			@Override
			public Schema schema() {
				return null;
			}

			@Override
			public SequenceNodeConfigurator configurator() {
				return null;
			}
		};
	}
}
