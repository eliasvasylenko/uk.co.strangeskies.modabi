package uk.co.strangeskies.modabi.schema.management.unbinding.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.io.BufferingDataTarget;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.io.DataTarget;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.management.unbinding.UnbindingException;
import uk.co.strangeskies.modabi.schema.node.DataNode;
import uk.co.strangeskies.modabi.schema.node.type.DataBindingType;
import uk.co.strangeskies.utilities.collection.computingmap.ComputingMap;

public class DataNodeUnbinder {
	private final UnbindingContextImpl context;

	public DataNodeUnbinder(UnbindingContextImpl context) {
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
	public <U> DataSource unbindToDataBuffer(DataNode.Effective<U> node,
			List<U> data) {
		BufferingDataTarget target = new BufferingDataTarget();

		UnbindingContextImpl context = this.context.withOutput(null).withProvision(
				DataTarget.class, c -> target);

		unbindWithFormat(node, data, null, context);

		return target.buffer();
	}

	private <U> void unbindWithFormat(DataNode.Effective<U> node, List<U> data,
			DataNode.Format format, UnbindingContextImpl context) {
		Map<QualifiedName, DataNode.Effective<?>> attemptedOverrideMap = new HashMap<>();

		BufferingDataTarget target = null;

		if (data != null) {
			if (node.isValueProvided()) {
				switch (node.valueResolution()) {
				case PROCESSING_TIME:

					break;
				case REGISTRATION_TIME:
					if (!node.providedValues().equals(data)) {
						throw new SchemaException("Provided value '"
								+ node.providedValues() + "'does not match unbinding object '"
								+ data + "' for node '" + node.getName() + "'.");
					}
					break;
				}
			} else {
				for (U item : data) {
					if (format != null) {
						target = new BufferingDataTarget();
						BufferingDataTarget finalTarget = target;
						context = context
								.withProvision(DataTarget.class, () -> finalTarget);
					}

					unbindToContext(node, item, context, attemptedOverrideMap);

					if (format != null) {
						DataSource bufferedTarget = target.buffer();

						if (bufferedTarget.size() > 0)
							switch (format) {
							case PROPERTY:
								bufferedTarget.pipe(
										context.output().writeProperty(node.getName())).terminate();
								break;
							case SIMPLE:
								context.output().nextChild(node.getName());
								bufferedTarget.pipe(context.output().writeContent())
										.terminate();
								context.output().endChild();
								break;
							case CONTENT:
								bufferedTarget.pipe(context.output().writeContent())
										.terminate();
							}
					}
				}
			}
		} else if (!node.optional())
			throw new SchemaException("Non-optional node '" + node.getName()
					+ "' cannot omit data for unbinding.");
	}

	private <U> void unbindToContext(DataNode.Effective<U> node, U data,
			UnbindingContextImpl context,
			Map<QualifiedName, DataNode.Effective<?>> attemptedOverrideMap) {
		if (node.isExtensible() != null && node.isExtensible()) {
			ComputingMap<DataBindingType.Effective<? extends U>, DataNode.Effective<? extends U>> overrides = context
					.getDataNodeOverrides(node);

			if (overrides.isEmpty())
				throw new SchemaException(
						"Unable to find concrete type to satisfy data node '"
								+ node.getName() + "' with type '"
								+ node.effective().type().getName() + "' for object '" + data
								+ "' to be unbound.");

			context.attemptUnbindingUntilSuccessful(
					overrides
							.keySet()
							.stream()
							.filter(
									m -> m.getDataType().rawClass()
											.isAssignableFrom(data.getClass()))
							.collect(Collectors.toList()),
					(c, n) -> unbindExactNode(context, overrides.putGet(n), data),
					l -> new UnbindingException("Unable to unbind data node '"
							+ node.getName()
							+ "' with type candidates '"
							+ overrides.keySet().stream()
									.map(m -> m.source().getName().toString())
									.collect(Collectors.joining(", ")) + "' for object '" + data
							+ "' to be unbound.", context, l));
		} else
			new BindingNodeUnbinder(context).unbind(node, data);
	}

	@SuppressWarnings("unchecked")
	private <U extends V, V> void unbindExactNode(UnbindingContextImpl context,
			DataNode.Effective<U> element, V data) {
		try {
			new BindingNodeUnbinder(context).unbind(element, (U) element
					.getDataType().rawClass().cast(data));
		} catch (ClassCastException e) {
			throw new UnbindingException("Cannot unbind data at this node.", context,
					e);
		}
	}
}
