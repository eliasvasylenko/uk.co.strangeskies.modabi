package uk.co.strangeskies.modabi.schema.processing.impl.unbinding;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.data.io.BufferingDataTarget;
import uk.co.strangeskies.modabi.data.io.DataSource;
import uk.co.strangeskies.modabi.data.io.DataTarget;
import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.model.building.impl.DataNodeWrapper;
import uk.co.strangeskies.modabi.schema.model.nodes.DataNode;
import uk.co.strangeskies.utilities.MultiException;

public class DataNodeUnbinder {
	private final UnbindingContext context;

	public DataNodeUnbinder(UnbindingContext context) {
		this.context = context;
	}

	public <U> void unbind(DataNode.Effective<U> node, List<U> data) {
		unbindWithFormat(node, data, node.format());
	}

	/**
	 * Doesn't output to context.output().
	 *
	 * @param node
	 * @param data
	 */
	public <U> void unbindToDataTarget(DataNode.Effective<U> node, List<U> data) {
		unbindWithFormat(node, data, null);
	}

	private <U> void unbindWithFormat(DataNode.Effective<U> node, List<U> data,
			DataNode.Format format) {
		UnbindingContext context = this.context;

		BufferingDataTarget target = null;

		if (format != null) {
			target = new BufferingDataTarget();
			BufferingDataTarget finalTarget = target;
			context = context.withProvision(DataTarget.class, () -> finalTarget);
		}

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
		else
			unbindToContext(node, data, context);

		if (format != null) {
			DataSource bufferedTarget = target.buffer();

			if (bufferedTarget.size() > 0)
				switch (format) {
				case PROPERTY:
					bufferedTarget.pipe(context.output().writeProperty(node.getName()))
							.terminate();
					break;
				case SIMPLE_ELEMENT:
					context.output().nextChild(node.getName());
					bufferedTarget.pipe(context.output().writeContent()).terminate();
					context.output().endChild();
					break;
				case CONTENT:
					bufferedTarget.pipe(context.output().writeContent()).terminate();
				}
		}
	}

	private <U> void unbindToContext(DataNode.Effective<U> node, List<U> data,
			UnbindingContext context) {
		if (data != null) {
			for (U item : data) {
				if (node.isExtensible() != null && node.isExtensible()) {
					List<DataNode.Effective<? extends U>> nodes = context
							.getMatchingTypes(node, item.getClass()).stream()
							.map(type -> new DataNodeWrapper<>(type.effective(), node))
							.collect(Collectors.toCollection(ArrayList::new));

					if (!node.isAbstract())
						nodes.add(node);

					if (nodes.isEmpty())
						throw new SchemaException(
								"Unable to find concrete type to satisfy data node '"
										+ node.getName() + "' with type '"
										+ node.effective().type().getName() + "' for object '"
										+ item + "' to be unbound.");

					new UnbindingAttempter(context).tryForEach(
							nodes,
							(c, n) -> new BindingNodeUnbinder(context).unbind(node, item),
							l -> new MultiException("Unable to unbind data node '"
									+ node.getName()
									+ "' with type candidates '"
									+ nodes.stream().map(m -> m.source().getName().toString())
											.collect(Collectors.joining(", ")) + "' for object '"
									+ item + "' to be unbound.", l));
				} else {
					new BindingNodeUnbinder(context).unbind(node, item);
				}
			}
		} else if (!node.optional())
			throw new SchemaException("Non-optional node '" + node.getName()
					+ "' cannot omit data for unbinding.");
	}
}
