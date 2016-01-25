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

import uk.co.strangeskies.mathematics.Range;
import uk.co.strangeskies.modabi.ReturningSchemaProcessor;
import uk.co.strangeskies.modabi.SchemaProcessor;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.utilities.IdentityProperty;

public interface ChildNode<S extends ChildNode<S, E>, E extends ChildNode.Effective<S, E>>
		extends SchemaNode<S, E> {
	interface Effective<S extends ChildNode<S, E>, E extends Effective<S, E>>
			extends ChildNode<S, E>, SchemaNode.Effective<S, E> {
		TypeToken<?> getPreInputType();

		void process(SchemaProcessor context);

		default <T> T process(ReturningSchemaProcessor<T> context) {
			IdentityProperty<T> result = new IdentityProperty<>();

			process(new SchemaProcessor() {
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

		@Override
		SchemaNode.Effective<?, ?> parent();
	}

	/**
	 * Default behaviour is as if 1..1.
	 *
	 * @return
	 */
	Range<Integer> occurrences();

	/**
	 * Default behaviour is as if true. If unordered, may input concurrently, and
	 * semantics of updating existing binding are more flexible. Also note that
	 * unordered nodes may bind and unbind with less memory-efficiency...
	 *
	 * @return
	 */
	Boolean isOrdered();

	TypeToken<?> getPostInputType();

	SchemaNode<?, ?> parent();
}
