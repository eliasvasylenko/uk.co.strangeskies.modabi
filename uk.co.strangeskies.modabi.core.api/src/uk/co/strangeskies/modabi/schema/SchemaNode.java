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

import uk.co.strangeskies.modabi.ModabiException;
import uk.co.strangeskies.modabi.NodeProcessor;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.ReturningNodeProcessor;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.reflection.Reified;
import uk.co.strangeskies.utilities.IdentityProperty;

/**
 * The base interface for {@link Schema schema} element nodes. Schemata are made
 * up of a number of {@link Model models} and {@link DataType data types}, which
 * are themselves a type of schema node, and the root elements of a graph of
 * schema nodes.
 * 
 * @author Elias N Vasylenko
 *
 * @param <S>
 */
public interface SchemaNode<S extends SchemaNode<S>> extends Reified<S> {
	/**
	 * @param processor
	 *          a processor to be called back upon in the manner of the visitor
	 *          pattern
	 */
	void process(NodeProcessor processor);

	/**
	 * @param processor
	 *          a processor to be called back upon in the manner of the visitor
	 *          pattern
	 * @return the value returned by execution of the processor
	 */
	default <T> T process(ReturningNodeProcessor<T> processor) {
		IdentityProperty<T> result = new IdentityProperty<>();

		process(new NodeProcessor() {
			@Override
			public <U> void accept(Model<U> node) {
				result.set(processor.accept(node));
			}

			@Override
			public <U> void accept(DataType<U> node) {
				result.set(processor.accept(node));
			}

			@Override
			public void accept(ChoiceNode node) {
				result.set(processor.accept(node));
			}

			@Override
			public void accept(SequenceNode node) {
				result.set(processor.accept(node));
			}

			@Override
			public void accept(InputSequenceNode node) {
				result.set(processor.accept(node));
			}

			@Override
			public <U> void accept(DataNode<U> node) {
				result.set(processor.accept(node));
			}

			@Override
			public <U> void accept(ComplexNode<U> node) {
				result.set(processor.accept(node));
			}
		});

		return result.get();
	}

	/**
	 * Get the schema node configurator which created this schema node, or in the
	 * case of a mutable configurator implementation, a copy thereof.
	 * 
	 * @return the creating configurator
	 */
	SchemaNodeConfigurator<?, S> configurator();

	boolean concrete();

	/**
	 * @return the fully qualified name of the schema node, which where applicable
	 *         typically corresponds with its serialized representation
	 */
	QualifiedName name();

	List<ChildNode<?>> children();

	default ChildNode<?> child(QualifiedName name) {
		return children().stream().filter(c -> c.name().equals(name)).findAny()
				.orElseThrow(() -> new ModabiException(t -> t.noChildFound(Arrays.asList(name), name(), children())));
	}

	default ChildNode<?> child(QualifiedName name, QualifiedName... names) {
		if (names.length == 0)
			return child(name);
		else
			return child(name).child(Arrays.asList(names));
	}

	default ChildNode<?> child(List<QualifiedName> names) {
		if (names.isEmpty())
			throw new ModabiException(t -> t.noChildFound(names, name(), children()));

		if (names.size() == 1)
			return child(names.get(0));
		else
			return child(names.get(0)).child(names.subList(1, names.size()));
	}

	default ChildNode<?> child(String name, String... names) {
		if (names.length == 0)
			return child(new QualifiedName(name, name().getNamespace()));
		else
			return child(name).child(names[0], Arrays.copyOfRange(names, 1, names.length));
	}

	BindingNode<?, ?> root();

	Schema schema();

	@Override
	default S copy() {
		return getThis();
	}
}
