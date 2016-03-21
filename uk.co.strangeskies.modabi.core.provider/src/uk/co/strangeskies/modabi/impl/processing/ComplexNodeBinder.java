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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.processing.BindingException;
import uk.co.strangeskies.modabi.schema.ComplexNode;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.Model.Effective;

public class ComplexNodeBinder<U> extends InputNodeBinder<ComplexNode.Effective<U>> {
	private final List<NodeBinding<U>> bindings;

	public ComplexNodeBinder(ProcessingContextImpl context, ComplexNode<U> node) {
		super(context, node.effective());

		bindings = bind();
	}

	public ComplexNodeBinder<U> bindToTarget() {
		for (NodeBinding<U> item : getBinding())
			invokeInMethod(item.getBinding());

		return this;
	}

	public List<NodeBinding<U>> getBinding() {
		return bindings;
	}

	@SuppressWarnings("unchecked")
	private List<NodeBinding<U>> bind() {
		ComplexNode.Effective<U> node = getNode();

		List<NodeBinding<U>> result = new ArrayList<>();

		repeatNode(count -> {
			ProcessingContextImpl context = getContext();
			ComplexNode.Effective<? extends U> exactNode = getExactNode(context, node);

			/*
			 * If the current node is not inline, we first determine whether the next
			 * input element is a match. If it is not, we break, then continue on to
			 * the next node if possible. If it is, then we must bind the next input
			 * element to this node.
			 */
			if (exactNode == null)
				return false;

			Function<ProcessingContextImpl, U> bind = c -> bindExactNode(c, exactNode);
			U binding;

			if (node.isInline() && !node.occurrences().isValueBelow(count)) {
				/*
				 * If the current node is inline we cannot predetermine whether the next
				 * input element matches by reading the name, so we must attempt to bind
				 * in a protected context, and revert on failure, then continue on to
				 * the next node if possible.
				 * 
				 * If we have not processed the minimum number of occurrences required
				 * for this node, we assume this binding to be a failure. If we have,
				 * then we assume the element we attempted to process was intended to be
				 * bound by the next node and continue.
				 */
				try {
					binding = context.attemptBinding(bind);
				} catch (Exception e) {
					return false;
				}
			} else {
				binding = bind.apply(context);
			}

			result.add(new NodeBinding<>(binding, exactNode));
			context.bindings().add((ComplexNode.Effective<U>) exactNode, binding);

			return true;
		});

		return result;
	}

	@SuppressWarnings("unchecked")
	protected ComplexNode.Effective<? extends U> getExactNode(ProcessingContextImpl context,
			ComplexNode.Effective<U> node) {
		QualifiedName nextElement = context.input().get().peekNextChild();

		ComplexNode.Effective<? extends U> exactNode;

		if (nextElement != null) {
			if (node.isExtensible()) {
				Model.Effective<?> extension = context.getModel(nextElement);

				if (extension == null) {
					throw new BindingException("Cannot find model '" + nextElement + "' to bind to", context);
				}

				exactNode = context.getComplexNodeOverrides(node).putGet((Effective<? extends U>) extension);
			} else if (node.isInline() || Objects.equals(nextElement, node.getName())) {
				exactNode = node;
			} else {
				exactNode = null;
			}
		} else {
			exactNode = null;
		}

		return exactNode;
	}

	protected U bindExactNode(ProcessingContextImpl context, ComplexNode.Effective<? extends U> node) {
		if (!node.isInline())
			context.input().get().startNextChild();

		U binding = new BindingNodeBinder(context).bind(node);

		if (!node.isInline())
			context.input().get().endChild();

		return binding;
	}
}
