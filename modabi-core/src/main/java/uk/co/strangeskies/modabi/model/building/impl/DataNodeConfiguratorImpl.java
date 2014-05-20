package uk.co.strangeskies.modabi.model.building.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import uk.co.strangeskies.modabi.data.DataBindingType;
import uk.co.strangeskies.modabi.data.io.BufferedDataSource;
import uk.co.strangeskies.modabi.model.building.ChildBuilder;
import uk.co.strangeskies.modabi.model.building.ChoiceNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.DataNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.ElementNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.InputSequenceNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.SequenceNodeConfigurator;
import uk.co.strangeskies.modabi.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.model.nodes.DataNode;
import uk.co.strangeskies.modabi.model.nodes.DataNodeChildNode;
import uk.co.strangeskies.modabi.model.nodes.DataNode.Format;
import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.processing.BindingStrategy;
import uk.co.strangeskies.modabi.schema.processing.UnbindingStrategy;

public class DataNodeConfiguratorImpl<T>
		extends
		BindingChildNodeConfiguratorImpl<DataNodeConfigurator<T>, DataNode<T>, T, DataNodeChildNode, DataNode<?>>
		implements DataNodeConfigurator<T> {
	protected static class DataNodeImpl<T> extends BindingChildNodeImpl<T>
			implements DataNode<T> {
		private final DataBindingType<T> type;
		private final BufferedDataSource value;
		private final Format format;
		private final Boolean optional;

		DataNodeImpl(DataNodeConfiguratorImpl<T> configurator) {
			super(configurator);

			format = configurator.format;
			type = configurator.type;
			optional = configurator.optional;

			value = configurator.value;

			if (super.getDataClass() != null
					&& !super.getDataClass().isAssignableFrom(type.getDataClass()))
				throw new SchemaException();

			if (super.getBindingStrategy() != null
					&& !super.getBindingStrategy().equals(type.getBindingStrategy()))
				throw new SchemaException();

			if (super.getBindingClass() != null
					&& !super.getBindingClass().equals(type.getBindingClass()))
				throw new SchemaException();

			if (super.getUnbindingStrategy() != null
					&& !super.getUnbindingStrategy().equals(type.getUnbindingStrategy()))
				throw new SchemaException();

			if (super.getUnbindingClass() != null
					&& !super.getUnbindingClass().equals(type.getUnbindingClass()))
				throw new SchemaException();

			if (super.getUnbindingMethod() != null
					&& !super.getUnbindingMethod().equals(type.getUnbindingMethod()))
				throw new SchemaException();
		}

		DataNodeImpl(DataNode<T> node, Collection<DataNode<T>> overriddenNodes,
				List<ChildNode> effectiveChildren, Class<?> outputTargetClass) {
			super(overrideWithType(node), overriddenNodes, effectiveChildren,
					outputTargetClass);

			type = getValue(node, overriddenNodes, n -> n.type());

			optional = getValue(node, overriddenNodes, n -> n.optional());

			format = getValue(node, overriddenNodes, n -> n.format(),
					(n, o) -> n == o);

			value = getValue(node, overriddenNodes, n -> n.value());
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof DataNode))
				return false;

			DataNode<?> other = (DataNode<?>) obj;

			return super.equals(obj) && Objects.equals(type, other.type())
					&& Objects.equals(value, other.value())
					&& Objects.equals(format, other.format())
					&& Objects.equals(optional, other.optional());
		}

		private static <T> DataNode<T> overrideWithType(DataNode<T> node) {
			node = DataNode.wrapType(node);
			return node;
		}

		@Override
		public final DataBindingType<T> type() {
			return type;
		}

		@Override
		public BufferedDataSource value() {
			return value;
		}

		@Override
		public final Format format() {
			return format;
		}

		@Override
		public final Boolean optional() {
			return optional;
		}
	}

	public Format format;

	private DataBindingType<T> type;
	private BufferedDataSource value;

	private Boolean optional;

	public DataNodeConfiguratorImpl(
			SchemaNodeConfigurationContext<? super DataNode<T>> parent) {
		super(parent);
	}

	protected final Class<?> getTypeBindingClass() {
		if (type != null)
			return type.getBindingClass();
		return getBindingClass();
	}

	protected final Class<?> getTypeUnbindingClass() {
		if (type != null)
			return type.getUnbindingClass();
		return getUnbindingClass();
	}

	@SuppressWarnings("unchecked")
	@Override
	public final <V extends T> DataNodeConfigurator<V> dataClass(
			Class<V> dataClass) {
		return (DataNodeConfigurator<V>) super.dataClass(dataClass);
	}

	@SuppressWarnings("unchecked")
	@Override
	public final <U extends T> DataNodeConfigurator<U> type(DataBindingType<U> type) {
		requireConfigurable(this.type);
		dataClass(type.getDataClass());
		this.type = (DataBindingType<T>) type;

		inheritChildren(type.getEffectiveChildren());

		return (DataNodeConfigurator<U>) getThis();
	}

	@Override
	protected void finaliseProperties() {
		if (!isFinalisedProperties()) {
			List<ChildNode> newInheritedChildren = new ArrayList<>();
			getOverriddenNodes().forEach(
					c -> c.getChildren().forEach(n -> newInheritedChildren.add(n)));

			inheritChildren(0, newInheritedChildren);
		}

		super.finaliseProperties();
	}

	@Override
	public DataNodeConfigurator<T> value(BufferedDataSource dataSource) {
		requireConfigurable(value);
		this.value = dataSource;

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

	@Override
	protected final DataNode<T> getEffective(DataNode<T> node) {
		return new DataNodeImpl<>(node, getOverriddenNodes(),
				getEffectiveChildren(), getContext().getCurrentChildOutputTargetClass());
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

	@Override
	public final Class<T> getDataClass() {
		if (type != null)
			return type.getDataClass();
		return super.getDataClass();
	}

	@Override
	public final BindingStrategy getBindingStrategy() {
		if (type != null)
			return type.getBindingStrategy();
		return super.getBindingStrategy();
	}

	@Override
	public final Class<?> getBindingClass() {
		if (type != null)
			return type.getBindingClass();
		return super.getBindingClass();
	}

	@Override
	public final UnbindingStrategy getUnbindingStrategy() {
		if (type != null)
			return type.getUnbindingStrategy();
		return super.getUnbindingStrategy();
	}

	@Override
	public final Class<?> getUnbindingClass() {
		if (type != null)
			return type.getUnbindingClass();
		return super.getUnbindingClass();
	}

	@Override
	public ChildBuilder<DataNodeChildNode, DataNode<?>> addChild() {
		SchemaNodeConfigurationContext<ChildNode> context = new SchemaNodeConfigurationContext<ChildNode>() {
			@Override
			public <U extends ChildNode> Set<U> overrideChild(String id,
					Class<U> nodeClass) {
				return DataNodeConfiguratorImpl.this.overrideChild(id, nodeClass);
			}

			@Override
			public Class<?> getCurrentChildOutputTargetClass() {
				return DataNodeConfiguratorImpl.this.getCurrentChildOutputTargetClass();
			}

			@Override
			public Class<?> getCurrentChildInputTargetClass() {
				return DataNodeConfiguratorImpl.this.getCurrentChildInputTargetClass();
			}

			@Override
			public void addChild(ChildNode result, ChildNode effective) {
				DataNodeConfiguratorImpl.this.addChild(result, effective);
			}
		};

		return new ChildBuilder<DataNodeChildNode, DataNode<?>>() {
			@Override
			public SequenceNodeConfigurator<DataNodeChildNode, DataNode<?>> sequence() {
				return new SequenceNodeConfiguratorImpl<>(context);
			}

			@Override
			public InputSequenceNodeConfigurator<DataNode<?>> inputSequence() {
				return new InputSequenceNodeConfiguratorImpl<>(context);
			}

			@Override
			public DataNodeConfigurator<Object> data() {
				return new DataNodeConfiguratorImpl<>(context);
			}

			@Override
			public ChoiceNodeConfigurator<DataNodeChildNode, DataNode<?>> choice() {
				return new ChoiceNodeConfiguratorImpl<>(context);
			}

			@Override
			public ElementNodeConfigurator<Object> element() {
				throw new UnsupportedOperationException();
			}
		};
	}
}
