/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
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
package uk.co.strangeskies.modabi.impl;

import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.SchemaProcessor;
import uk.co.strangeskies.modabi.schema.BindingChildNode;
import uk.co.strangeskies.modabi.schema.ChoiceNode;
import uk.co.strangeskies.modabi.schema.ComplexNode;
import uk.co.strangeskies.modabi.schema.DataNode;
import uk.co.strangeskies.modabi.schema.InputNode;
import uk.co.strangeskies.modabi.schema.InputSequenceNode;
import uk.co.strangeskies.modabi.schema.SchemaNode;
import uk.co.strangeskies.modabi.schema.SequenceNode;

public interface PartialSchemaProcessor extends SchemaProcessor {
	@Override
	default <U> void accept(ComplexNode.Effective<U> node) {
		accept((BindingChildNode.Effective<U, ?, ?>) node);
	}

	@Override
	default <U> void accept(DataNode.Effective<U> node) {
		accept((BindingChildNode.Effective<U, ?, ?>) node);
	}

	@Override
	default void accept(InputSequenceNode.Effective node) {
		accept((InputNode.Effective<?, ?>) node);
	}

	@Override
	default void accept(SequenceNode.Effective node) {
		accept((SchemaNode.Effective<?, ?>) node);
	}

	@Override
	default void accept(ChoiceNode.Effective node) {
		accept((SchemaNode.Effective<?, ?>) node);
	}

	default <U> void accept(BindingChildNode.Effective<U, ?, ?> node) {
		accept((InputNode.Effective<?, ?>) node);
	}

	default void accept(InputNode.Effective<?, ?> node) {
		accept((SchemaNode.Effective<?, ?>) node);
	}

	default void accept(SchemaNode.Effective<?, ?> node) {
		throw new SchemaException("Unexpected node type '" + node.getClass()
				+ "' for node '" + node.getName() + "'");
	}
}
