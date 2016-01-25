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
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.schema.ChildNode;
import uk.co.strangeskies.modabi.schema.SchemaNode;
import uk.co.strangeskies.modabi.schema.SequenceNode;
import uk.co.strangeskies.reflection.TypeToken;

public class DummyNodes {
	private DummyNodes() {}

	public static SequenceNode sequenceNode(String name, String... children) {
		return sequenceNode(new QualifiedName(name), Arrays.asList(children)
				.stream().map(DummyNodes::sequenceNode).collect(Collectors.toList()));
	}

	public static SequenceNode sequenceNode(QualifiedName name,
			QualifiedName... children) {
		return sequenceNode(name, Arrays.asList(children).stream()
				.map(DummyNodes::sequenceNode).collect(Collectors.toList()));
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
				if (!(object instanceof SequenceNode))
					return false;

				if (object == this)
					return true;

				return ((SequenceNode) object).getName().equals(getName())
						&& children.equals(((SequenceNode) object).children());
			}

			@Override
			public int hashCode() {
				return getName().hashCode() + children.hashCode();
			}

			@Override
			public TypeToken<?> getPostInputType() {
				return TypeToken.over(Object.class);
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
						if (!(object instanceof SequenceNode.Effective))
							return false;

						return source().equals(((SequenceNode) object).source());
					}

					@Override
					public Boolean isAbstract() {
						return false;
					}

					@Override
					public boolean hasExtensibleChildren() {
						return false;
					}

					@Override
					public int hashCode() {
						return getName().hashCode() + children.hashCode();
					}

					@Override
					public List<ChildNode.Effective<?, ?>> children() {
						return children.stream().<ChildNode
								.Effective<?, ?>> map(c -> c.effective())
								.collect(Collectors.toList());
					}

					@Override
					public SequenceNode source() {
						return thisNode;
					}

					@Override
					public TypeToken<?> getPreInputType() {
						return TypeToken.over(Object.class);
					}

					@Override
					public TypeToken<?> getPostInputType() {
						return TypeToken.over(Object.class);
					}

					@Override
					public Range<Integer> occurrences() {
						return Range.between(1, 1);
					}

					@Override
					public Boolean isOrdered() {
						return true;
					}

					@Override
					public SchemaNode.Effective<?, ?> parent() {
						return null;
					}
				};
			}

			@Override
			public Range<Integer> occurrences() {
				return Range.between(1, 1);
			}

			@Override
			public Boolean isOrdered() {
				return true;
			}

			@Override
			public SchemaNode<?, ?> parent() {
				return null;
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
			public boolean hasExtensibleChildren() {
				return false;
			}

			@Override
			public boolean equals(Object object) {
				if (!(object instanceof SequenceNode.Effective))
					return false;

				return ((SequenceNode) object).getName().equals(getName());
			}

			@Override
			public int hashCode() {
				return getName().hashCode();
			}

			@Override
			public TypeToken<?> getPreInputType() {
				return TypeToken.over(Object.class);
			}

			@Override
			public TypeToken<?> getPostInputType() {
				return TypeToken.over(Object.class);
			}

			@Override
			public Range<Integer> occurrences() {
				return Range.between(1, 1);
			}

			@Override
			public Boolean isOrdered() {
				return true;
			}

			@Override
			public SchemaNode.Effective<?, ?> parent() {
				return null;
			}
		};
	}
}
