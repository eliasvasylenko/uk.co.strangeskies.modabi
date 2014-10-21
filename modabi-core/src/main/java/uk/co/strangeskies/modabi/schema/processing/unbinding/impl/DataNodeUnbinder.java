package uk.co.strangeskies.modabi.schema.processing.unbinding.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.io.BufferingDataTarget;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.io.DataTarget;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.node.DataNode;
import uk.co.strangeskies.modabi.schema.node.type.DataBindingType;
import uk.co.strangeskies.modabi.schema.node.type.DataBindingTypeBuilder;
import uk.co.strangeskies.modabi.schema.processing.impl.BindingNodeOverrider;
import uk.co.strangeskies.modabi.schema.processing.unbinding.UnbindingException;

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

		if (node.isValueProvided())
			switch (node.valueResolution()) {
			case PROCESSING_TIME:

				break;
			case REGISTRATION_TIME:
				if (!node.providedValues().equals(data)) {
					throw new SchemaException("Provided value '" + node.providedValues()
							+ "'does not match unbinding object '" + data + "' for node '"
							+ node.getName() + "'.");
				}
				break;
			}
		else if (data != null) {
			for (U item : data) {
				if (format != null) {
					target = new BufferingDataTarget();
					BufferingDataTarget finalTarget = target;
					context = context.withProvision(DataTarget.class, () -> finalTarget);
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
							bufferedTarget.pipe(context.output().writeContent()).terminate();
							context.output().endChild();
							break;
						case CONTENT:
							bufferedTarget.pipe(context.output().writeContent()).terminate();
						}
				}
			}
		} else if (!node.optional())
			throw new SchemaException("Non-optional node '" + node.getName()
					+ "' cannot omit data for unbinding.");
	}

	@SuppressWarnings("unchecked")
	private <U> void unbindToContext(DataNode.Effective<U> node, U data,
			UnbindingContextImpl context,
			Map<QualifiedName, DataNode.Effective<?>> attemptedOverrideMap) {
		if (node.isExtensible() != null && node.isExtensible()) {
			List<? extends DataBindingType.Effective<? extends U>> nodes = context
					.getMatchingTypes(node, data.getClass());

			if (nodes.isEmpty())
				throw new SchemaException(
						"Unable to find concrete type to satisfy data node '"
								+ node.getName() + "' with type '"
								+ node.effective().type().getName() + "' for object '" + data
								+ "' to be unbound.");

			/* DataNode.Effective<? extends U> success = */
			context
					.attemptUnbindingUntilSuccessful(
							nodes,
							(c, n) -> {
								DataNode.Effective<? extends U> overridden = (DataNode.Effective<? extends U>) attemptedOverrideMap
										.get(n.getName());

								if (overridden == null) {
									overridden = new BindingNodeOverrider().override(c
											.provisions().provide(DataBindingTypeBuilder.class),
											node, n);
									attemptedOverrideMap.put(n.getName(), overridden);
								}

								unbindExactNode(context, overridden, data);
							}, l -> new UnbindingException("Unable to unbind data node '"
									+ node.getName()
									+ "' with type candidates '"
									+ nodes.stream().map(m -> m.source().getName().toString())
											.collect(Collectors.joining(", ")) + "' for object '"
									+ data + "' to be unbound.", context, l));

			/*-
			 * TODO allow optimisation in unambiguous cases? Currently breaks precedence.
			nodes.remove(success);
			((List<Object>) nodes).add(0, success);
			 */
		} else
			new BindingNodeUnbinder(context).unbind(node, data);
	}

	private <U extends V, V> void unbindExactNode(UnbindingContextImpl context,
			DataNode.Effective<U> element, V data) {
		try {
			new BindingNodeUnbinder(context).unbind(element, element.getDataClass()
					.cast(data));
		} catch (ClassCastException e) {
			throw new UnbindingException("Cannot unbind data at this node.", context,
					e);
		}
	}
}
