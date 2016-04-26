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

import java.util.List;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.processing.ProcessingContext;
import uk.co.strangeskies.modabi.processing.ProcessingException;
import uk.co.strangeskies.modabi.schema.ComplexNode;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.utilities.collection.computingmap.ComputingMap;

public class ComplexNodeUnbinder {
	private final ProcessingContext context;

	public ComplexNodeUnbinder(ProcessingContext context) {
		this.context = context;
	}

	@SuppressWarnings("unchecked")
	public <U> void unbind(ComplexNode.Effective<U> node, List<U> data) {
		ProcessingContextImpl context = new ProcessingContextImpl(this.context);

		if (node.extensible()) {
			for (U item : data) {
				ComputingMap<Model<? extends U>, ComplexNode.Effective<? extends U>> overrides = context
						.getComplexNodeOverrides(node);

				List<Model<? extends U>> validOverrides = overrides.keySet().stream()
						.filter(m -> m.effective().dataType().getRawType().isAssignableFrom(item.getClass()))
						.collect(Collectors.toList());

				if (validOverrides.isEmpty()) {
					throw new ProcessingException(
							"Unable to find model to satisfy complex node '" + node.name() + "' with base model '"
									+ node.model().stream().map(m -> m.source().name().toString()).collect(Collectors.joining(", "))
									+ "' for object '" + item + "' to be unbound",
							context);
				}

				context
						.attemptUnbindingUntilSuccessful(validOverrides,
								(c, n) -> unbindExactNode(c, (ComplexNode.Effective<U>) overrides.putGet(n), item),
								l -> new ProcessingException(
										"Unable to unbind complex node '" + node.name() + "' with model candidates '"
												+ validOverrides.stream().map(m -> m.effective().name().toString())
														.collect(Collectors.joining(", "))
												+ "' for object '" + item + "' to be unbound",
										context, l));
			}
		} else {
			for (U item : data) {
				unbindExactNode(context, node, item);
			}
		}
	}

	private <U> void unbindExactNode(ProcessingContextImpl context, ComplexNode.Effective<U> element, U data) {
		try {
			try {
				if (!element.inline())
					context.output().get().addChild(element.name());

				new BindingNodeUnbinder(context).unbind(element, data);

				if (!element.inline())
					context.output().get().endChild();

				context.bindings().add(element, data);
			} catch (ClassCastException e) {
				throw new ProcessingException("Cannot unbind data at this node.", context, e);
			}
		} catch (Exception e) {
			throw e;
		}
	}
}
