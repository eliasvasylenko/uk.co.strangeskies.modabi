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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.ReturningSchemaProcessor;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.NodeProcessor;
import uk.co.strangeskies.utilities.IdentityProperty;

public interface SchemaNode<S extends SchemaNode<S, E>, E extends SchemaNode.Effective<S, E>> {
	interface Effective<S extends SchemaNode<S, E>, E extends Effective<S, E>> extends SchemaNode<S, E> {
		@Override
		List<ChildNode.Effective<?, ?>> children();

		@SuppressWarnings("unchecked")
		@Override
		default E effective() {
			return (E) this;
		}

		@Override
		S source();

		boolean hasExtensibleChildren();

		@Override
		BindingNode.Effective<?, ?, ?> root();

		void process(NodeProcessor context);

		default <T> T process(ReturningSchemaProcessor<T> context) {
			IdentityProperty<T> result = new IdentityProperty<>();

			process(new NodeProcessor() {
				@Override
				public <U> void accept(Model.Effective<U> node) {
					result.set(context.accept(node));
				}

				@Override
				public <U> void accept(DataType.Effective<U> node) {
					result.set(context.accept(node));
				}

				@Override
				public void accept(ChoiceNode.Effective node) {
					result.set(context.accept(node));
				}

				@Override
				public void accept(SequenceNode.Effective node) {
					result.set(context.accept(node));
				}

				@Override
				public void accept(InputSequenceNode.Effective node) {
					result.set(context.accept(node));
				}

				@Override
				public <U> void accept(DataNode.Effective<U> node) {
					result.set(context.accept(node));
				}

				@Override
				public <U> void accept(ComplexNode.Effective<U> node) {
					result.set(context.accept(node));
				}
			});

			return result.get();
		}
	}

	Boolean isAbstract();

	QualifiedName getName();

	List<? extends ChildNode<?, ?>> children();

	E effective();

	@SuppressWarnings("unchecked")
	default S source() {
		return (S) this;
	}

	default ChildNode<?, ?> child(QualifiedName name) {
		return children().stream().filter(c -> c.getName().equals(name)).findAny()
				.orElseThrow(() -> new SchemaException(
						"Cannot find child '" + name + "' for node '" + getName() + "' amongst children '["
								+ children().stream().map(SchemaNode::getName).map(Objects::toString).collect(Collectors.joining(", "))
								+ "]."));
	}

	default ChildNode<?, ?> child(QualifiedName name, QualifiedName... names) {
		if (names.length == 0)
			return child(name);
		else
			return child(name).child(Arrays.asList(names));
	}

	default ChildNode<?, ?> child(List<QualifiedName> names) {
		if (names.isEmpty())
			throw new IllegalArgumentException();

		if (names.size() == 1)
			return child(names.get(0));
		else
			return child(names.get(0)).child(names.subList(1, names.size()));
	}

	default ChildNode<?, ?> child(String name, String... names) {
		if (names.length == 0)
			return child(new QualifiedName(name, getName().getNamespace()));
		else
			return child(name).child(names[0], Arrays.copyOfRange(names, 1, names.length));
	}

	BindingNode<?, ?, ?> root();

	Schema schema();
}
