package uk.co.strangeskies.modabi.impl.schema;

import java.util.List;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.ValueResolution;
import uk.co.strangeskies.modabi.impl.schema.utilities.OverrideMerge;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.schema.DataBindingType;
import uk.co.strangeskies.modabi.schema.DataNode;
import uk.co.strangeskies.utilities.PropertySet;

public class DataNodeImpl<T> extends
		BindingChildNodeImpl<T, DataNode<T>, DataNode.Effective<T>> implements
		DataNode<T> {
	public static class Effective<T> extends
			BindingChildNodeImpl.Effective<T, DataNode<T>, DataNode.Effective<T>>
			implements DataNode.Effective<T> {
		private final DataBindingType.Effective<T> type;
		private final Format format;
		private final Boolean optional;
		private final Boolean nullIfOmitted;
		private final DataSource providedBuffer;
		private final ValueResolution resolution;
		private List<T> provided;

		protected Effective(
				OverrideMerge<DataNode<T>, DataNodeConfiguratorImpl<T>> overrideMerge) {
			super(overrideMerge);

			DataBindingType<T> type = overrideMerge.tryGetValue(DataNode::type,
					(n, o) -> {
						DataBindingType<?> p = n.effective();
						do
							if (p == o.effective())
								return true;
						while ((p = p.baseType().effective()) != null);
						return false;
					});
			this.type = type == null ? null : type.effective();

			format = overrideMerge.tryGetValue(DataNode::format);
			if (format != null
					&& overrideMerge.configurator().getContext().isInputDataOnly())
				throw new SchemaException("Node '" + getName()
						+ "' must not provide a format.");

			optional = overrideMerge.getValue(DataNode::optional, (n, o) -> o || !n,
					false);

			nullIfOmitted = overrideMerge.getValue(DataNode::nullIfOmitted,
					(n, o) -> o || !n, false);

			if (!isAbstract()
					&& nullIfOmitted
					&& (!optional || format == Format.SIMPLE || !overrideMerge
							.configurator().getContext().isInputExpected()))
				throw new SchemaException(
						"'Null if omitted' property is not valid for node '" + getName()
								+ "'");

			providedBuffer = overrideMerge.tryGetValue(DataNode::providedValueBuffer);
			ValueResolution resolution = overrideMerge.getValue(
					DataNode::valueResolution, ValueResolution.PROCESSING_TIME);

			if (providedBuffer == null
					&& resolution == ValueResolution.REGISTRATION_TIME && !isAbstract()
					&& !optional)
				throw new SchemaException(
						"Value must be provided at registration time for node '"
								+ getName() + "'");

			provided = (resolution == ValueResolution.REGISTRATION_TIME && providedBuffer != null) ? overrideMerge
					.configurator().getContext().dataLoader()
					.loadData(DataNodeImpl.Effective.this, providedBuffer)
					: null;

			this.resolution = resolution;
		}

		@Override
		protected QualifiedName defaultName(
				OverrideMerge<DataNode<T>, ? extends SchemaNodeConfiguratorImpl<?, DataNode<T>>> overrideMerge) {
			DataBindingType<T> type = overrideMerge.tryGetValue(DataNode::type,
					(o, n) -> true);
			return type == null ? null : type.getName();
		}

		@Override
		public final DataBindingType.Effective<T> type() {
			return type;
		}

		@Override
		public final Format format() {
			return format;
		}

		@Override
		public final Boolean optional() {
			return optional;
		}

		@Override
		public final Boolean nullIfOmitted() {
			return nullIfOmitted;
		}

		@Override
		public DataSource providedValueBuffer() {
			return providedBuffer == null ? null : providedBuffer.copy().reset();
		}

		@Override
		public List<T> providedValues() {
			return provided;
		}

		@Override
		public ValueResolution valueResolution() {
			return resolution;
		}
	}

	private final Effective<T> effective;

	private final DataBindingType<T> type;
	private final Format format;
	private final Boolean optional;
	private final Boolean nullIfOmitted;
	private final DataSource providedBuffer;
	private final ValueResolution resolution;

	DataNodeImpl(DataNodeConfiguratorImpl<T> configurator) {
		super(configurator);

		format = configurator.getFormat();
		type = configurator.getType();
		optional = configurator.getOptional();
		nullIfOmitted = configurator.getNullIfOmitted();

		providedBuffer = configurator.getProvidedBufferedValue();
		resolution = configurator.getResolution();

		effective = new Effective<>(DataNodeConfiguratorImpl.overrideMerge(this,
				configurator));
	}

	@SuppressWarnings("rawtypes")
	protected static final PropertySet<DataNode> PROPERTY_SET = new PropertySet<>(
			DataNode.class).add(BindingChildNodeImpl.PROPERTY_SET)
			.add(DataNode::format).add(DataNode::providedValueBuffer)
			.add(DataNode::valueResolution).add(DataNode::type)
			.add(DataNode::optional).add(DataNode::isExtensible)
			.add(DataNode::nullIfOmitted);

	@SuppressWarnings("unchecked")
	@Override
	protected PropertySet<DataNode<T>> propertySet() {
		return (PropertySet<DataNode<T>>) (Object) PROPERTY_SET;
	}

	@Override
	public final DataBindingType<T> type() {
		return type;
	}

	@Override
	public final Format format() {
		return format;
	}

	@Override
	public final Boolean optional() {
		return optional;
	}

	@Override
	public final Boolean nullIfOmitted() {
		return nullIfOmitted;
	}

	@Override
	public DataSource providedValueBuffer() {
		return providedBuffer == null ? null : providedBuffer.copy().reset();
	}

	@Override
	public ValueResolution valueResolution() {
		return resolution;
	}

	@Override
	public DataNodeImpl.Effective<T> effective() {
		return effective;
	}
}
