package uk.co.strangeskies.modabi.schema.processing.impl.unbinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.data.DataBindingType;
import uk.co.strangeskies.modabi.data.io.BufferingDataTarget;
import uk.co.strangeskies.modabi.data.io.DataSource;
import uk.co.strangeskies.modabi.data.io.DataTarget;
import uk.co.strangeskies.modabi.data.io.structured.StructuredDataTarget;
import uk.co.strangeskies.modabi.schema.Bindings;
import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.model.Model;
import uk.co.strangeskies.modabi.schema.model.building.impl.DataNodeWrapper;
import uk.co.strangeskies.modabi.schema.model.nodes.DataNode;
import uk.co.strangeskies.modabi.schema.model.nodes.ElementNode;
import uk.co.strangeskies.modabi.schema.model.nodes.SchemaNode;

public class DataNodeUnbinder {
	private final UnbindingContext context;

	public DataNodeUnbinder(UnbindingContext context) {
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

		UnbindingContext context = new UnbindingContext() {
			@Override
			public Object unbindingSource() {
				return DataNodeUnbinder.this.context.unbindingSource();
			}

			@Override
			public List<SchemaNode.Effective<?, ?>> unbindingNodeStack() {
				return Collections.emptyList();
			}

			@SuppressWarnings("unchecked")
			@Override
			public <T> T provide(Class<T> clazz, UnbindingContext context) {
				if (clazz.equals(DataTarget.class))
					return (T) target;

				return DataNodeUnbinder.this.context.provide(clazz);
			}

			@Override
			public boolean isProvided(Class<?> clazz) {
				return clazz.equals(DataTarget.class)
						|| DataNodeUnbinder.this.context.isProvided(clazz);
			}

			@Override
			public StructuredDataTarget output() {
				return null;
			}

			@Override
			public <T> List<Model<? extends T>> getMatchingModels(
					ElementNode.Effective<T> element, Class<?> dataClass) {
				return null;
			}

			@Override
			public <T> List<DataBindingType<? extends T>> getMatchingTypes(
					DataNode.Effective<T> node, Class<?> dataClass) {
				return DataNodeUnbinder.this.context.getMatchingTypes(node, dataClass);
			}

			@Override
			public Bindings bindings() {
				return DataNodeUnbinder.this.context.bindings();
			}
		};

		unbindWithFormat(node, data, null, context);

		return target.buffer();
	}

	private <U> void unbindWithFormat(DataNode.Effective<U> node, List<U> data,
			DataNode.Format format, UnbindingContext context) {
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

				unbindToContext(node, item, context);

				if (format != null) {
					DataSource bufferedTarget = target.buffer();

					if (bufferedTarget.size() > 0)
						switch (format) {
						case PROPERTY:
							bufferedTarget.pipe(
									context.output().writeProperty(node.getName())).terminate();
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
		} else if (!node.optional())
			throw new SchemaException("Non-optional node '" + node.getName()
					+ "' cannot omit data for unbinding.");
	}

	private <U> void unbindToContext(DataNode.Effective<U> node, U data,
			UnbindingContext context) {
		if (node.isExtensible() != null && node.isExtensible()) {
			List<DataNode.Effective<? extends U>> nodes = context
					.getMatchingTypes(node, data.getClass()).stream()
					.map(type -> new DataNodeWrapper<>(type.effective(), node))
					.collect(Collectors.toCollection(ArrayList::new));

			if (!node.isAbstract())
				nodes.add(node);

			if (nodes.isEmpty())
				throw new SchemaException(
						"Unable to find concrete type to satisfy data node '"
								+ node.getName() + "' with type '"
								+ node.effective().type().getName() + "' for object '" + data
								+ "' to be unbound.");

			new UnbindingAttempter(context).tryForEach(
					nodes,
					(c, n) -> new BindingNodeUnbinder(context).unbind(node, data),
					l -> context.exception(
							"Unable to unbind data node '"
									+ node.getName()
									+ "' with type candidates '"
									+ nodes.stream().map(m -> m.source().getName().toString())
											.collect(Collectors.joining(", ")) + "' for object '"
									+ data + "' to be unbound.", l));
		} else {
			new BindingNodeUnbinder(context).unbind(node, data);
		}
	}
}
