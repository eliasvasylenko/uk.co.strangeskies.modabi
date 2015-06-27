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
package uk.co.strangeskies.modabi.impl.processing;

import java.util.List;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.processing.UnbindingException;
import uk.co.strangeskies.modabi.schema.ComplexNode;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.utilities.collection.computingmap.ComputingMap;

public class ComplexNodeUnbinder {
	private final UnbindingContextImpl context;

	public ComplexNodeUnbinder(UnbindingContextImpl context) {
		this.context = context;
	}

	public <U> void unbind(ComplexNode.Effective<U> node, List<U> data) {
		if (node.isExtensible()) {
			for (U item : data) {
				ComputingMap<Model<? extends U>, ComplexNode.Effective<? extends U>> overrides = context
						.getComplexNodeOverrides(node);

				List<Model<? extends U>> validOverrides = overrides
						.keySet()
						.stream()
						.filter(
								m -> m.effective().getDataType().getRawType()
										.isAssignableFrom(item.getClass()))
						.collect(Collectors.toList());

				if (validOverrides.isEmpty())
					throw new UnbindingException(
							"Unable to find model to satisfy complex node '"
									+ node.getName()
									+ "' with base model '"
									+ node.baseModel().stream()
											.map(m -> m.source().getName().toString())
											.collect(Collectors.joining(", ")) + "' for object '"
									+ item + "' to be unbound", context);

				context.attemptUnbindingUntilSuccessful(
						validOverrides,
						(c, n) -> unbindExactNode(c, overrides.putGet(n), item),
						l -> new UnbindingException("Unable to unbind complex node '"
								+ node.getName()
								+ "' with model candidates '"
								+ validOverrides.stream()
										.map(m -> m.effective().getName().toString())
										.collect(Collectors.joining(", ")) + "' for object '"
								+ item + "' to be unbound", context, l));
			}
		} else
			for (U item : data)
				unbindExactNode(context, node, item);
	}

	@SuppressWarnings("unchecked")
	private <U extends V, V> void unbindExactNode(UnbindingContextImpl context,
			ComplexNode.Effective<U> element, V data) {
		try {
			if (!element.isInline())
				context.output().nextChild(element.getName());

			new BindingNodeUnbinder(context).unbind(element, (U) element
					.getDataType().getRawType().cast(data));

			if (!element.isInline())
				context.output().endChild();

			context.bindings().add(element, data);
		} catch (ClassCastException e) {
			throw new UnbindingException("Cannot unbind data at this node.", context,
					e);
		}
	}
}
