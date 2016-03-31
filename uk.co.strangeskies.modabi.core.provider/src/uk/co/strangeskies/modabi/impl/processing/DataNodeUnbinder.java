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

import uk.co.strangeskies.modabi.Provider;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.io.BufferingDataTarget;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.io.DataTarget;
import uk.co.strangeskies.modabi.processing.ProcessingException;
import uk.co.strangeskies.modabi.schema.DataNode;
import uk.co.strangeskies.modabi.schema.DataType;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.utilities.collection.computingmap.ComputingMap;

public class DataNodeUnbinder {
	private final ProcessingContextImpl context;

	public DataNodeUnbinder(ProcessingContextImpl context) {
		this.context = context;
	}

	public <U> void unbind(DataNode.Effective<U> node, List<U> data) {
		unbindWithFormat(node, data, node.format(), context);
	}

	/**
	 * Doesn't output to context.output().
	 *
	 * @param node
	 * @param data
	 */
	public <U> DataSource unbindToDataBuffer(DataNode.Effective<U> node, List<U> data) {
		BufferingDataTarget target = new BufferingDataTarget();

		ProcessingContextImpl context = this.context.withOutput(null).withProvisionScope();
		context.provisions().add(Provider.over(new TypeToken<DataTarget>() {}, c -> target));

		unbindWithFormat(node, data, null, context);

		return target.buffer();
	}

	private <U> void unbindWithFormat(DataNode.Effective<U> node, List<U> data, DataNode.Format format,
			ProcessingContextImpl context) {
		Map<QualifiedName, DataNode.Effective<?>> attemptedOverrideMap = new HashMap<>();

		BufferingDataTarget target = null;

		if (data != null) {
			if (node.isValueProvided()) {
				switch (node.valueResolution()) {
				case PROCESSING_TIME:

					break;
				case REGISTRATION_TIME:
				case POST_REGISTRATION:
					if (!node.providedValues().equals(data)) {
						throw new SchemaException("Provided value '" + node.providedValues() + "'does not match unbinding object '"
								+ data + "' for node '" + node.getName() + "'");
					}
					break;
				}
			} else {
				for (U item : data) {
					if (format != null) {
						target = new BufferingDataTarget();
						BufferingDataTarget finalTarget = target;
						context = context.withProvisionScope();
						context.provisions().add(Provider.over(new TypeToken<DataTarget>() {}, () -> finalTarget));
					}

					unbindToContext(node, item, context, attemptedOverrideMap);

					if (format != null) {
						DataSource bufferedTarget = target.buffer();

						if (bufferedTarget.size() > 0)
							switch (format) {
							case PROPERTY:
								bufferedTarget.pipe(context.output().get().writeProperty(node.getName())).terminate();
								break;
							case SIMPLE:
								context.output().get().addChild(node.getName());
								bufferedTarget.pipe(context.output().get().writeContent()).terminate();
								context.output().get().endChild();
								break;
							case CONTENT:
								bufferedTarget.pipe(context.output().get().writeContent()).terminate();
							}
					}
				}
			}
		} else if (!node.occurrences().contains(0))
			throw new SchemaException("Non-optional node '" + node.getName() + "' cannot omit data for unbinding");
	}

	private <U> void unbindToContext(DataNode.Effective<U> node, U data, ProcessingContextImpl context,
			Map<QualifiedName, DataNode.Effective<?>> attemptedOverrideMap) {
		if (node.isExtensible() != null && node.isExtensible()) {
			ComputingMap<DataType<? extends U>, DataNode.Effective<? extends U>> overrides = context
					.getDataNodeOverrides(node);

			if (node.isAbstract() && overrides.isEmpty()) {
				throw new SchemaException("Unable to find concrete type to satisfy data node '" + node.getName()
						+ "' with type '" + node.effective().type().getName() + "' for object '" + data + "' to be unbound");
			}

			try {
				context
						.<DataType<? extends U>> attemptUnbindingUntilSuccessful(
								overrides.keySet().stream()
										.filter(m -> m.effective().getDataType().getRawType().isAssignableFrom(data.getClass()))
										.collect(Collectors.toList()),
								(c, n) -> unbindExactNode(context, overrides.putGet(n), data),
								l -> new ProcessingException(
										"Unable to unbind data node '" + node.getName() + "' with type candidates '"
												+ overrides.keySet().stream().map(m -> m.effective().getName().toString())
														.collect(Collectors.joining(", "))
												+ "' for object '" + data + "' to be unbound",
										context, l));
			} catch (ProcessingException e) {
				if (!node.isAbstract()) {
					new BindingNodeUnbinder(context).unbind(node, data);
				} else {
					throw new ProcessingException("Could not unbind without extension", context, e);
				}
			}
		} else
			new BindingNodeUnbinder(context).unbind(node, data);
	}

	@SuppressWarnings("unchecked")
	private <U extends V, V> void unbindExactNode(ProcessingContextImpl context, DataNode.Effective<U> element, V data) {
		try {
			new BindingNodeUnbinder(context).unbind(element, (U) element.getDataType().getRawType().cast(data));
		} catch (ClassCastException e) {
			throw new ProcessingException("Cannot unbind data at this node.", context, e);
		}
	}
}
