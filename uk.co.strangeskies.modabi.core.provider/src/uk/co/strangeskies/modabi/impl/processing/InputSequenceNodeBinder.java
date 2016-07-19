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
package uk.co.strangeskies.modabi.impl.processing;

import java.util.function.Consumer;

import uk.co.strangeskies.modabi.ChildNodeBinding;
import uk.co.strangeskies.modabi.schema.InputSequenceNode;

public class InputSequenceNodeBinder extends InputNodeBinder<InputSequenceNode> {
	public InputSequenceNodeBinder(ProcessingContextImpl context, InputSequenceNode node) {
		super(context, node);

		Consumer<ProcessingContextImpl> bind = c -> invokeInMethod(
				BindingNodeBinder.getSingleBindingSequence(node, c).stream().map(ChildNodeBinding::getData).toArray());

		repeatNode(count -> {
			if (node.occurrences().isValueBelow(count)) {
				bind.accept(context);
			} else {
				try {
					context.attemptBinding(bind);
				} catch (RuntimeException e) {
					return e;
				}
			}
			return null;
		});
	}
}
