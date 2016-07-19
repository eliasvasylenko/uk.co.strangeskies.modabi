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

import java.util.function.Function;

import uk.co.strangeskies.modabi.ReturningNodeProcessor;
import uk.co.strangeskies.modabi.processing.ProcessingContext;
import uk.co.strangeskies.modabi.processing.ProcessingException;
import uk.co.strangeskies.modabi.schema.ChildNode;
import uk.co.strangeskies.modabi.schema.ChoiceNode;
import uk.co.strangeskies.modabi.schema.ComplexNode;
import uk.co.strangeskies.modabi.schema.DataNode;
import uk.co.strangeskies.modabi.schema.InputSequenceNode;
import uk.co.strangeskies.modabi.schema.SequenceNode;

public abstract class ChildNodeBinder<T extends ChildNode<?>> {
	private ProcessingContext context;
	private final T node;

	public ChildNodeBinder(ProcessingContext context, T node) {
		this.context = context;
		this.node = node;
	}

	protected void setContext(ProcessingContext context) {
		this.context = context;
	}

	public ProcessingContextImpl getContext() {
		return new ProcessingContextImpl(context);
	}

	public T getNode() {
		return node;
	}

	protected <E extends Exception> void repeatNode(Function<Integer, E> repeatAtCount) throws E {
		int count = 0;
		E exception = null;

		try {
			do {
				exception = repeatAtCount.apply(count);
				if (exception != null) {
					break;
				}

				count++;
			} while (!node.occurrences().isValueAbove(count + 1));
		} catch (Exception e) {
			throw new ProcessingException("Node '" + node.name() + "' failed to bind on occurance '" + count + "' of range '"
					+ node.occurrences() + "'", context, e);
		}

		if (!node.occurrences().contains(count)) {
			throw new ProcessingException(
					"Node '" + node.name() + "' occurrences '" + count + "' should be within range '" + node.occurrences() + "'",
					getContext(), exception);
		}
	}

	public static ProcessingContextImpl bind(ProcessingContextImpl parentContext, ChildNode<?> next) {
		ProcessingContextImpl context = parentContext.withBindingNode(next);

		ReturningNodeProcessor<ChildNodeBinder<?>> childProcessor = new ReturningNodeProcessor<ChildNodeBinder<?>>() {
			@Override
			public <U> ChildNodeBinder<?> accept(ComplexNode<U> node) {
				return new ComplexNodeBinder<>(context, node).bindToTarget();
			}

			@Override
			public <U> ChildNodeBinder<?> accept(DataNode<U> node) {
				return new DataNodeBinder<>(context, node).bindToTarget();
			}

			@Override
			public ChildNodeBinder<?> accept(InputSequenceNode node) {
				return new InputSequenceNodeBinder(context, node);
			}

			@Override
			public ChildNodeBinder<?> accept(SequenceNode node) {
				return new SequenceNodeBinder(context, node);
			}

			@Override
			public ChildNodeBinder<?> accept(ChoiceNode node) {
				return new ChoiceNodeBinder(context, node);
			}
		};

		try {
			return parentContext.withReplacementBindingObject(next.process(childProcessor).getContext().getBindingObject());
		} catch (Exception e) {
			throw new ProcessingException("Failed to bind node '" + next + "'", context, e);
		}
	}
}
