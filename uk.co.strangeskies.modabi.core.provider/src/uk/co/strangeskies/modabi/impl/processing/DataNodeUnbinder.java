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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.ModabiException;
import uk.co.strangeskies.modabi.Provider;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.io.BufferingDataTarget;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.io.DataTarget;
import uk.co.strangeskies.modabi.processing.ProcessingContext;
import uk.co.strangeskies.modabi.processing.ProcessingException;
import uk.co.strangeskies.modabi.schema.SimpleNode;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.utilities.collection.computingmap.ComputingMap;

public class DataNodeUnbinder {
	private final ProcessingContextImpl context;

	public DataNodeUnbinder(ProcessingContext context) {
		this(new ProcessingContextImpl(context));
	}

	public DataNodeUnbinder(ProcessingContextImpl context) {
		this.context = context;
	}

	public <U> void unbind(SimpleNode<U> node, List<U> data) {
		unbindWithFormat(node, data, node.format(), context);
	}

	/**
	 * Doesn't output to context.output().
	 *
	 * @param node
	 * @param data
	 */
	public <U> DataSource unbindToDataBuffer(SimpleNode<U> node) {
		ProcessingContextImpl context = this.context.withOutput(null).withNestedProvisionScope();

		List<U> data = BindingNodeUnbinder.getData(node, context);

		BufferingDataTarget target = new BufferingDataTarget();

		context.provisions().add(Provider.over(new TypeToken<DataTarget>() {}, c -> target));

		unbindWithFormat(node, data, null, context);

		return target.buffer();
	}

	private <U> void unbindWithFormat(SimpleNode<U> node, List<U> data, SimpleNode.Format format,
			ProcessingContextImpl context) {
		Map<QualifiedName, SimpleNode<?>> attemptedOverrideMap = new HashMap<>();

		BufferingDataTarget target = null;

		if (node.isValueProvided()) {
			switch (node.valueResolution()) {
			case PROCESSING_TIME:

				break;
			case REGISTRATION_TIME:
			case POST_REGISTRATION:
				if (!node.providedValues().equals(data)) {
					throw new ModabiException("Provided value '" + node.providedValues() + "'does not match unbinding object '"
							+ data + "' for node '" + node.name() + "'");
				}
				break;
			}
		} else {
			for (U item : data) {
				if (format != null) {
					target = new BufferingDataTarget();
					BufferingDataTarget finalTarget = target;
					context = context.withNestedProvisionScope();
					context.provisions().add(Provider.over(new TypeToken<DataTarget>() {}, () -> finalTarget));
				}

				unbindToContext(node, item, context, attemptedOverrideMap);

				if (format != null) {
					DataSource bufferedTarget = target.buffer();

					if (bufferedTarget.size() > 0)
						switch (format) {
						case PROPERTY:
							bufferedTarget.pipe(context.output().get().writeProperty(node.name())).terminate();
							break;
						case SIMPLE:
							context.output().get().addChildBindingPoint(node.name());
							bufferedTarget.pipe(context.output().get().writeContent()).terminate();
							context.output().get().endChild();
							break;
						case CONTENT:
							bufferedTarget.pipe(context.output().get().writeContent()).terminate();
						}
				}
			}
		}
	}

	private <U> void unbindToContext(SimpleNode<U> node, U data, ProcessingContextImpl context,
			Map<QualifiedName, SimpleNode<?>> attemptedOverrideMap) {
		if (node.extensible() != null && node.extensible()) {
			ComputingMap<SimpleNode<? extends U>, SimpleNode<? extends U>> overrides = context.getDataNodeOverrides(node);

			if (overrides.isEmpty()) {
				throw new ModabiException("Unable to find concrete type to satisfy data node '" + node.name() + "' with type '"
						+ node.type().name() + "' for object '" + data + "' to be unbound");
			}

			context
					.<SimpleNode<? extends U>>attemptUnbindingUntilSuccessful(
							overrides.keySet().stream().filter(m -> m.getDataType().getRawType().isAssignableFrom(data.getClass()))
									.collect(Collectors.toList()),
							(c, n) -> unbindExactNode(context, overrides.putGet(n), data),
							l -> new ProcessingException("Unable to unbind data node '" + node.name() + "' with type candidates '"
									+ overrides.keySet().stream().map(m -> m.name().toString()).collect(Collectors.joining(", "))
									+ "' for object '" + data + "' to be unbound", context, l));
		} else
			new BindingNodeUnbinder(context, node, data).unbind();
	}

	@SuppressWarnings("unchecked")
	private <U extends V, V> void unbindExactNode(ProcessingContextImpl context, SimpleNode<U> element, V data) {
		try {
			new BindingNodeUnbinder(context, element, (U) element.getDataType().getRawType().cast(data)).unbind();
		} catch (ClassCastException e) {
			throw new ProcessingException("Cannot unbind data at this node.", context, e);
		}
	}
}
