package uk.co.strangeskies.modabi.schema.processing.impl.unbinding;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.data.io.BufferingDataTarget;
import uk.co.strangeskies.modabi.data.io.DataSource;
import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.model.building.impl.DataNodeWrapper;
import uk.co.strangeskies.modabi.schema.model.nodes.DataNode;
import uk.co.strangeskies.utilities.MultiException;

public class DataNodeUnbinder {
	private final UnbindingContext context;

	public DataNodeUnbinder(UnbindingContext context) {
		this.context = context;
	}

	@SuppressWarnings("unchecked")
	public <U> void unbind(DataNode.Effective<U> node) {
	}

	public <U> void unbindNode(DataNode.Effective<U> node) {
		nodeStack.push(node);
		if (node.getOutMethodName() == null
				|| !node.getOutMethodName().equals("null")) {
			if (dataTarget == null) {
				if (node.format() == null)
					throw new SchemaException(
							"Data format must be provided for data node '" + node.getName()
									+ "'.");
				dataTarget = new BufferingDataTarget();
			} else if (node.format() != null)
				throw new SchemaException(
						"Data format should be null for nested data node '"
								+ node.getName() + "'.");

			if (node.isValueProvided())
				switch (node.valueResolution()) {
				case PROCESSING_TIME:

					break;
				case REGISTRATION_TIME:
					List<U> data = getData(node);

					if (!node.providedValues().equals(data)) {
						throw new SchemaException("Provided value '"
								+ node.providedValues() + "'does not match unbinding object '"
								+ data + "' for node '" + node.getName() + "'.");
					}
					break;
				}
			else
				unbindDataNode(node, dataTarget);

			if (node.format() != null) {
				DataSource bufferedTarget = dataTarget.buffer();
				dataTarget = null;

				if (bufferedTarget.size() > 0)
					switch (node.format()) {
					case PROPERTY:
						bufferedTarget.pipe(output.writeProperty(node.getName()))
								.terminate();
						break;
					case SIMPLE_ELEMENT:
						output.nextChild(node.getName());
						bufferedTarget.pipe(output.writeContent()).terminate();
						output.endChild();
						break;
					case CONTENT:
						bufferedTarget.pipe(output.writeContent()).terminate();
					}
			}
		}
		nodeStack.pop();
	}

	public <U> BufferingDataTarget unbindDataNode(DataNode.Effective<U> node,
			BufferingDataTarget target) {
		List<U> data = getData(node);

		if (data != null) {
			for (U item : data) {
				BufferingDataTarget previousDataTarget = dataTarget;
				dataTarget = target;

				if (node.isExtensible() != null && node.isExtensible()) {
					List<DataNode.Effective<? extends U>> nodes = this.schemaBinderImpl.registeredTypes
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

				dataTarget = previousDataTarget;
			}
		} else if (!node.optional())
			throw new SchemaException("Non-optional node '" + node.getName()
					+ "' cannot omit data for unbinding.");

		return target;
	}
}
