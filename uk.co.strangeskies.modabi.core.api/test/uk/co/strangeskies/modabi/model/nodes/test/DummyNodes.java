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
package uk.co.strangeskies.modabi.model.nodes.test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.node.ChildNode;
import uk.co.strangeskies.modabi.schema.node.SchemaNode;
import uk.co.strangeskies.modabi.schema.node.SequenceNode;

public class DummyNodes {
	private DummyNodes() {
	}

	public static SequenceNode sequenceNode(String name, String... children) {
		return sequenceNode(new QualifiedName(name), Arrays.asList(children)
				.stream().map(DummyNodes::sequenceNode).collect(Collectors.toList()));
	}

	public static SequenceNode sequenceNode(QualifiedName name,
			QualifiedName... children) {
		return sequenceNode(
				name,
				Arrays.asList(children).stream().map(DummyNodes::sequenceNode)
						.collect(Collectors.toList()));
	}

	public static SequenceNode sequenceNode(String name,
			ChildNode<?, ?>... children) {
		return sequenceNode(new QualifiedName(name), children);
	}

	public static SequenceNode sequenceNode(QualifiedName name,
			ChildNode<?, ?>... children) {
		return sequenceNode(name, Arrays.asList(children));
	}

	public static SequenceNode sequenceNode(QualifiedName name,
			List<? extends ChildNode<?, ?>> children) {
		return new SequenceNode() {
			@Override
			public QualifiedName getName() {
				return name;
			}

			@Override
			public Boolean isAbstract() {
				return false;
			}

			@Override
			public List<? extends ChildNode<?, ?>> children() {
				return children;
			}

			@Override
			public boolean equals(Object object) {
				return propertySet().testEquality(object)
						&& effective().equals(((SchemaNode<?, ?>) object).effective());
			}

			@Override
			public int hashCode() {
				return propertySet().generateHashCode();
			}

			@Override
			public Class<?> getPostInputType() {
				return Object.class;
			}

			@Override
			public Effective effective() {
				SequenceNode thisNode = this;

				return new Effective() {
					@Override
					public QualifiedName getName() {
						return name;
					}

					@Override
					public boolean equals(Object object) {
						return propertySet().testEquality(object)
								&& effectivePropertySet().testEquality(object);
					}

					@Override
					public Boolean isAbstract() {
						return false;
					}

					@Override
					public int hashCode() {
						return propertySet().generateHashCode()
								^ effectivePropertySet().generateHashCode();
					}

					@Override
					public List<ChildNode.Effective<?, ?>> children() {
						/*
						 * TODO Yet another compiler bug to report? Should be able to supply
						 * SchemaNode::effective as the argument to map, and leave out the
						 * explicit parametrisation,but javac gets upset.
						 */
						return children.stream()
								.<ChildNode.Effective<?, ?>> map(c -> c.effective())
								.collect(Collectors.toList());
					}

					@Override
					public SequenceNode source() {
						return thisNode;
					}

					@Override
					public Class<?> getPreInputType() {
						return Object.class;
					}

					@Override
					public Class<?> getPostInputType() {
						return Object.class;
					}
				};
			}
		};
	}

	public static SequenceNode sequenceNode(String name) {
		return sequenceNode(new QualifiedName(name));
	}

	public static SequenceNode sequenceNode(QualifiedName name) {
		return new SequenceNode.Effective() {
			@Override
			public QualifiedName getName() {
				return name;
			}

			@Override
			public List<ChildNode.Effective<?, ?>> children() {
				return Collections.emptyList();
			}

			@Override
			public SequenceNode source() {
				return this;
			}

			@Override
			public Boolean isAbstract() {
				return false;
			}

			@Override
			public boolean equals(Object object) {
				return propertySet().testEquality(object)
						&& effectivePropertySet().testEquality(object);
			}

			@Override
			public int hashCode() {
				return propertySet().generateHashCode()
						^ effectivePropertySet().generateHashCode();
			}

			@Override
			public Class<?> getPreInputType() {
				return Object.class;
			}

			@Override
			public Class<?> getPostInputType() {
				return Object.class;
			}
		};
	}
}
