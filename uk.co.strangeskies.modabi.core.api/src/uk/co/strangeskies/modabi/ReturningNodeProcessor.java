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

import static uk.co.strangeskies.utilities.text.Localizer.getDefaultLocalizer;

import uk.co.strangeskies.modabi.schema.ChoiceNode;
import uk.co.strangeskies.modabi.schema.ComplexNode;
import uk.co.strangeskies.modabi.schema.DataNode;
import uk.co.strangeskies.modabi.schema.DataType;
import uk.co.strangeskies.modabi.schema.InputSequenceNode;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.SchemaNode;
import uk.co.strangeskies.modabi.schema.SequenceNode;

public interface ReturningNodeProcessor<T> {
	default <U> T accept(DataType.Effective<U> node) {
		return acceptDefault(node);
	}

	default <U> T accept(Model.Effective<U> node) {
		return acceptDefault(node);
	}

	default <U> T accept(ComplexNode.Effective<U> node) {
		return acceptDefault(node);
	}

	default <U> T accept(DataNode.Effective<U> node) {
		return acceptDefault(node);
	}

	default T accept(InputSequenceNode.Effective node) {
		return acceptDefault(node);
	}

	default T accept(SequenceNode.Effective node) {
		return acceptDefault(node);
	}

	default T accept(ChoiceNode.Effective node) {
		return acceptDefault(node);
	}

	default T acceptDefault(SchemaNode.Effective<?, ?> node) {
		throw new ModabiException(getDefaultLocalizer().getLocalization(ModabiExceptionText.class).unexpectedNodeType());
	}
}
