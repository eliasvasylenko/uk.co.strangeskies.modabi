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
package uk.co.strangeskies.modabi;

import uk.co.strangeskies.modabi.schema.ChoiceNode;
import uk.co.strangeskies.modabi.schema.ComplexNode;
import uk.co.strangeskies.modabi.schema.DataNode;
import uk.co.strangeskies.modabi.schema.DataType;
import uk.co.strangeskies.modabi.schema.InputSequenceNode;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.SchemaNode;
import uk.co.strangeskies.modabi.schema.SequenceNode;

public interface NodeProcessor {
	default <U> void accept(DataType<U> node) {
		acceptDefault(node);
	}

	default <U> void accept(Model<U> node) {
		acceptDefault(node);
	}

	default <U> void accept(ComplexNode<U> node) {
		acceptDefault(node);
	}

	default <U> void accept(DataNode<U> node) {
		acceptDefault(node);
	}

	default void accept(InputSequenceNode node) {
		acceptDefault(node);
	}

	default void accept(SequenceNode node) {
		acceptDefault(node);
	}

	default void accept(ChoiceNode node) {
		acceptDefault(node);
	}

	default void acceptDefault(SchemaNode<?> node) {
		throw new ModabiException(t -> t.unexpectedNodeType());
	}
}
