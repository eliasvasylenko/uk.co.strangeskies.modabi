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
package uk.co.strangeskies.modabi.impl.schema;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.Abstractness;
import uk.co.strangeskies.modabi.NodeProcessor;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.impl.schema.utilities.OverrideMerge;
import uk.co.strangeskies.modabi.schema.ChildNode;
import uk.co.strangeskies.modabi.schema.ChoiceNode;
import uk.co.strangeskies.modabi.schema.ComplexNode;
import uk.co.strangeskies.modabi.schema.DataNode;
import uk.co.strangeskies.modabi.schema.InputSequenceNode;
import uk.co.strangeskies.modabi.schema.SchemaNode;
import uk.co.strangeskies.modabi.schema.SequenceNode;

public abstract class SchemaNodeImpl<S extends SchemaNode<S, E>, E extends SchemaNode.Effective<S, E>>
		implements SchemaNode<S, E> {
	protected static abstract class Effective<S extends SchemaNode<S, E>, E extends SchemaNode.Effective<S, E>>
			implements SchemaNode.Effective<S, E> {
		private final S source;

		private final QualifiedName name;
		private final Abstractness abstractness;
		private final List<ChildNode.Effective<?, ?>> children;

		protected Effective(OverrideMerge<S, ? extends SchemaNodeConfiguratorImpl<?, S>> overrideMerge) {
			source = overrideMerge.node().source();

			name = overrideMerge.getOverride(SchemaNode::name).orDefault(overrideMerge.configurator().defaultName())
					.validate((n, o) -> true).get();

			abstractness = overrideMerge.node().abstractness() == null ? Abstractness.CONCRETE
					: overrideMerge.node().abstractness();

			children = overrideMerge.configurator().getChildrenContainer().getEffectiveChildren();

			if (!overrideMerge.configurator().isChildContextAbstract())
				requireNonAbstractDescendents(new ArrayDeque<>(Arrays.asList(this)));
		}

		protected void requireNonAbstractDescendents(Deque<SchemaNode.Effective<?, ?>> nodeStack) {
			for (ChildNode.Effective<?, ?> child : nodeStack.peek().children()) {
				nodeStack.push(child);

				child.process(new NodeProcessor() {
					@Override
					public void accept(ChoiceNode.Effective node) {
						requireNonAbstract(nodeStack);
					}

					@Override
					public void accept(SequenceNode.Effective node) {
						requireNonAbstract(nodeStack);
					}

					@Override
					public void accept(InputSequenceNode.Effective node) {
						requireNonAbstract(nodeStack);
					}

					@Override
					public <U> void accept(DataNode.Effective<U> node) {
						if (node.extensible() == null || !node.extensible())
							requireNonAbstract(nodeStack);
					}

					@Override
					public <U> void accept(ComplexNode.Effective<U> node) {
						if (node.extensible() == null || !node.extensible())
							requireNonAbstract(nodeStack);
					}
				});

				nodeStack.pop();
			}
		}

		protected void requireNonAbstract(Deque<SchemaNode.Effective<?, ?>> nodeStack) {
			if (nodeStack.peek().abstractness().isMoreThan(Abstractness.UNINFERRED))
				throw new SchemaException("Inherited descendent '"
						+ nodeStack.stream().map(n -> n.name().toString()).collect(Collectors.joining(" < "))
						+ "' must be overridden");

			requireNonAbstractDescendents(nodeStack);
		}

		@Override
		public S source() {
			return source;
		}

		@Override
		public QualifiedName name() {
			return name;
		}

		@Override
		public Abstractness abstractness() {
			return abstractness;
		}

		@Override
		public List<ChildNode.Effective<?, ?>> children() {
			return children;
		}

		@Override
		public boolean equals(Object that) {
			return that instanceof SchemaNode.Effective && Objects.equals(name(), ((SchemaNode.Effective<?, ?>) that).name());
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(name());
		}

		@Override
		public String toString() {
			return name() != null ? name().toString() : "[Unnamed Node]";
		}

		@Override
		public Schema schema() {
			return root().schema();
		}
	}

	private final QualifiedName name;
	private final Abstractness abstractness;
	private final List<ChildNode<?, ?>> children;

	protected SchemaNodeImpl(SchemaNodeConfiguratorImpl<?, ?> configurator) {
		configurator.finaliseConfiguration();
		configurator.finaliseChildren();

		name = configurator.getName();

		abstractness = configurator.abstractness();

		children = Collections.unmodifiableList(new ArrayList<>(configurator.getChildrenContainer().getChildren()));
	}

	@Override
	public final QualifiedName name() {
		return name;
	}

	@Override
	public Abstractness abstractness() {
		return abstractness;
	}

	@Override
	public final List<? extends ChildNode<?, ?>> children() {
		return children;
	}

	@Override
	public boolean equals(Object that) {
		return that instanceof SchemaNode && Objects.equals(name(), ((SchemaNode<?, ?>) that).name());
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(name());
	}

	@Override
	public String toString() {
		return effective().name().toString();
	}

	@Override
	public Schema schema() {
		return root().schema();
	}
}
