package uk.co.strangeskies.modabi.schema.node.building.configuration.impl;

import java.util.ArrayList;
import java.util.List;

import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.node.DataNode;
import uk.co.strangeskies.modabi.schema.node.DataNode.Format;
import uk.co.strangeskies.modabi.schema.node.DataNodeChildNode;
import uk.co.strangeskies.modabi.schema.node.building.configuration.DataNodeConfigurator;
import uk.co.strangeskies.modabi.schema.node.building.configuration.impl.utilities.OverrideMerge;
import uk.co.strangeskies.modabi.schema.node.building.configuration.impl.utilities.SchemaNodeConfigurationContext;
import uk.co.strangeskies.modabi.schema.node.type.DataBindingType;
import uk.co.strangeskies.modabi.schema.node.wrapping.impl.DataNodeWrapper;
import uk.co.strangeskies.modabi.schema.processing.ValueResolution;

public class DataNodeConfiguratorImpl<T>
		extends
		BindingChildNodeConfiguratorImpl<DataNodeConfigurator<T>, DataNode<T>, T, DataNodeChildNode<?, ?>, DataNode<?>>
		implements DataNodeConfigurator<T> {
	protected static class DataNodeImpl<T> extends
			BindingChildNodeImpl<T, DataNode<T>, DataNode.Effective<T>> implements
			DataNode<T> {
		private static class Effective<T> extends
				BindingChildNodeImpl.Effective<T, DataNode<T>, DataNode.Effective<T>>
				implements DataNode.Effective<T> {
			private final DataBindingType.Effective<T> type;
			private final Format format;
			private final Boolean optional;
			private final DataSource providedBuffer;
			private final ValueResolution resolution;
			private List<T> provided;

			protected Effective(
					OverrideMerge<DataNode<T>, DataNodeConfiguratorImpl<T>> overrideMerge) {
				super(overrideMerge);

				DataBindingType<T> type = overrideMerge.tryGetValue(DataNode::type, (n,
						o) -> {
					DataBindingType<?> p = n.effective();
					do
						if (p == o.effective())
							return true;
					while ((p = p.baseType().effective()) != null);
					return false;
				});
				this.type = type == null ? null : type.effective();

				optional = overrideMerge.getValue(DataNode::optional,
						(n, o) -> o || !n, false);

				providedBuffer = overrideMerge
						.tryGetValue(DataNode::providedValueBuffer);
				resolution = overrideMerge.getValue(DataNode::valueResolution,
						ValueResolution.PROCESSING_TIME);

				format = overrideMerge.tryGetValue(DataNode::format);
				if (format == null) {
					if (!isAbstract()
							&& valueResolution() != ValueResolution.REGISTRATION_TIME
							&& !overrideMerge.configurator().getContext().isInputDataOnly()) {
						/*- TODO not all nodes actually request input!
						 * Deal with nodes which don't need format...
						 *
						throw new SchemaException("Node '" + getName()
								+ "' must provide a format.");
						 */
					}
				} else if (overrideMerge.configurator().getContext().isInputDataOnly()) {
					throw new SchemaException("Node '" + getName()
							+ "' must not provide a format.");
				}

				if (providedBuffer == null
						&& resolution == ValueResolution.REGISTRATION_TIME && !isAbstract()
						&& optional == false)
					throw new SchemaException(
							"Value must be provided at registration time for node '"
									+ getName() + "'.");

				provided = (resolution == ValueResolution.REGISTRATION_TIME && providedBuffer != null) ? overrideMerge
						.configurator().getContext().dataLoader()
						.loadData(DataNodeImpl.Effective.this, providedBuffer)
						: null;
			}

			@Override
			protected QualifiedName defaultName(
					OverrideMerge<DataNode<T>, ? extends SchemaNodeConfiguratorImpl<?, DataNode<T>, ?, ?>> overrideMerge) {
				DataBindingType<T> type = overrideMerge.tryGetValue(DataNode::type, (o,
						n) -> true);
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
		private final DataSource providedBuffer;
		private final ValueResolution resolution;

		DataNodeImpl(DataNodeConfiguratorImpl<T> configurator) {
			super(configurator);

			format = configurator.format;
			type = configurator.type;
			optional = configurator.optional;

			providedBuffer = configurator.providedBufferedValue;
			resolution = configurator.resolution;

			effective = new Effective<>(overrideMerge(this, configurator));
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

	public Format format;

	private DataBindingType<T> type;
	private DataSource providedBufferedValue;
	private ValueResolution resolution;

	private Boolean optional;

	public DataNodeConfiguratorImpl(
			SchemaNodeConfigurationContext<? super DataNode<T>> parent) {
		super(parent);
	}

	@Override
	public DataNodeConfigurator<T> name(String name) {
		return name(new QualifiedName(name, getContext().namespace()));
	}

	@SuppressWarnings("unchecked")
	@Override
	public final <V extends T> DataNodeConfigurator<V> dataClass(
			Class<V> dataClass) {
		return (DataNodeConfigurator<V>) super.dataClass(dataClass);
	}

	@SuppressWarnings("unchecked")
	@Override
	public final <U extends T> DataNodeConfigurator<U> type(
			DataBindingType<? super U> type) {
		requireConfigurable(this.type);
		this.type = (DataBindingType<T>) type;

		return (DataNodeConfigurator<U>) getThis();
	}

	@Override
	public List<DataNode<T>> getOverriddenNodes() {
		List<DataNode<T>> overriddenNodes = new ArrayList<>();

		if (type != null)
			overriddenNodes.add(new DataNodeWrapper<>(type.effective()));

		overriddenNodes.addAll(super.getOverriddenNodes());

		return overriddenNodes;
	}

	@Override
	protected boolean isDataContext() {
		return true;
	}

	@Override
	public DataNodeConfigurator<T> provideValue(DataSource dataSource) {
		requireConfigurable(providedBufferedValue);
		providedBufferedValue = dataSource;

		return getThis();
	}

	@Override
	public DataNodeConfigurator<T> valueResolution(ValueResolution valueResolution) {
		requireConfigurable(this.resolution);
		this.resolution = valueResolution;

		return getThis();
	}

	@Override
	public final DataNodeConfigurator<T> optional(boolean optional) {
		requireConfigurable(this.optional);
		this.optional = optional;

		return this;
	}

	@Override
	public final DataNodeConfigurator<T> format(Format format) {
		requireConfigurable(this.format);
		this.format = format;

		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected final Class<DataNode<T>> getNodeClass() {
		return (Class<DataNode<T>>) (Object) DataNode.class;
	}

	@Override
	protected final DataNode<T> tryCreate() {
		return new DataNodeImpl<>(this);
	}
}
