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
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

import uk.co.strangeskies.modabi.Abstractness;
import uk.co.strangeskies.modabi.ModabiException;
import uk.co.strangeskies.modabi.NodeProcessor;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.schema.ChildNode;
import uk.co.strangeskies.modabi.schema.ChoiceNode;
import uk.co.strangeskies.modabi.schema.ComplexNode;
import uk.co.strangeskies.modabi.schema.DataNode;
import uk.co.strangeskies.modabi.schema.InputSequenceNode;
import uk.co.strangeskies.modabi.schema.SchemaNode;
import uk.co.strangeskies.modabi.schema.SchemaNodeConfigurator;
import uk.co.strangeskies.modabi.schema.SequenceNode;

public abstract class SchemaNodeImpl<S extends SchemaNode<S>> implements SchemaNode<S> {
	private final SchemaNodeConfiguratorImpl<?, S> configurator;

	private final QualifiedName name;
	private final Abstractness abstractness;
	private final List<ChildNode<?>> children;

	protected SchemaNodeImpl(SchemaNodeConfiguratorImpl<?, S> configurator) {
		this.configurator = configurator;
		configurator.finaliseConfiguration();
		configurator.finaliseChildren();

		name = configurator.getOverride(SchemaNode::name).orDefault(configurator.defaultName()).validate((n, o) -> true)
				.get();

		abstractness = configurator.abstractness() == null ? Abstractness.CONCRETE : configurator.abstractness();

		children = configurator.getChildren();

		if (!configurator.isChildContextAbstract())
			requireNonAbstractDescendents(new ArrayDeque<>(Arrays.asList(this)));
	}

	protected void requireNonAbstractDescendents(Deque<SchemaNode<?>> nodeStack) {
		for (ChildNode<?> child : nodeStack.peek().children()) {
			nodeStack.push(child);

			child.process(new NodeProcessor() {
				@Override
				public void accept(ChoiceNode node) {
					requireNonAbstract(nodeStack);
				}

				@Override
				public void accept(SequenceNode node) {
					requireNonAbstract(nodeStack);
				}

				@Override
				public void accept(InputSequenceNode node) {
					requireNonAbstract(nodeStack);
				}

				@Override
				public <U> void accept(DataNode<U> node) {
					if (node.extensible() == null || !node.extensible())
						requireNonAbstract(nodeStack);
				}

				@Override
				public <U> void accept(ComplexNode<U> node) {
					if (node.extensible() == null || !node.extensible())
						requireNonAbstract(nodeStack);
				}
			});

			nodeStack.pop();
		}
	}

	protected void requireNonAbstract(Deque<SchemaNode<?>> nodeStack) {
		if (nodeStack.peek().abstractness().isMoreThan(Abstractness.UNINFERRED))
			throw new ModabiException(t -> t.mustOverrideDescendant(nodeStack));

		requireNonAbstractDescendents(nodeStack);
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
	public List<ChildNode<?>> children() {
		return children;
	}

	@Override
	public boolean equals(Object that) {
		return that instanceof SchemaNode<?> && Objects.equals(name(), ((SchemaNode<?>) that).name());
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
	public SchemaNodeConfigurator<?, S> configurator() {
		return configurator;
	}
}
