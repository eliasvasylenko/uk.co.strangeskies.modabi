package uk.co.strangeskies.modabi.model.impl;

import java.util.Collection;
import java.util.List;

import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.data.DataSource;
import uk.co.strangeskies.modabi.data.DataType;
import uk.co.strangeskies.modabi.model.building.DataNodeConfigurator;
import uk.co.strangeskies.modabi.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.model.nodes.DataNode;
import uk.co.strangeskies.modabi.model.nodes.DataNode.Format;
import uk.co.strangeskies.modabi.processing.SchemaProcessingContext;
import uk.co.strangeskies.modabi.processing.SchemaResultProcessingContext;

public class DataNodeConfiguratorImpl<T> extends
		BindingChildNodeConfiguratorImpl<DataNodeConfigurator<T>, DataNode<T>, T>
		implements DataNodeConfigurator<T> {
	protected static class DataNodeImpl<T> extends BindingChildNodeImpl<T>
			implements DataNode<T> {
		private final DataType<T> type;
		private final T value;
		private final Format format;
		private final Boolean optional;

		DataNodeImpl(DataNodeConfiguratorImpl<T> configurator) {
			super(configurator);

			format = configurator.format;
			type = configurator.type;
			value = configurator.value;
			optional = configurator.optional;
		}

		DataNodeImpl(DataNode<T> node, Collection<DataNode<T>> overriddenNodes,
				List<ChildNode> effectiveChildren, Class<?> parentClass) {
			super(node, overriddenNodes, effectiveChildren, parentClass);

			type = getValue(node, overriddenNodes, n -> n.type());

			value = getValue(node, overriddenNodes, n -> n.value());

			optional = getValue(node, overriddenNodes, n -> n.optional());

			format = getValue(node, overriddenNodes, n -> n.format(),
					(n, o) -> n == o);

			if (value != null && !getDataClass().isAssignableFrom(value.getClass()))
				throw new SchemaException();
		}

		@Override
		public final DataType<T> type() {
			return type;
		}

		@Override
		public final T value() {
			return value;
		}

		@Override
		public final void process(SchemaProcessingContext context) {
			context.accept(this);
		}

		@Override
		public final <R> R process(SchemaResultProcessingContext<R> context) {
			return context.accept(this);
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

	private DataType<T> type;
	private T value;

	private Boolean optional;

	public DataNodeConfiguratorImpl(
			SchemaNodeConfigurationContext<? super DataNode<T>> parent) {
		super(parent);
	}

	@SuppressWarnings("unchecked")
	@Override
	public final <V extends T> DataNodeConfigurator<V> dataClass(
			Class<V> dataClass) {
		return (DataNodeConfigurator<V>) super.dataClass(dataClass);
	}

	@SuppressWarnings("unchecked")
	@Override
	public final <U extends T> DataNodeConfigurator<U> type(DataType<U> type) {
		requireConfigurable(this.type);
		dataClass(type.getDataClass());
		this.type = (DataType<T>) type;

		return (DataNodeConfigurator<U>) getThis();
	}

	@Override
	public final DataNodeConfigurator<T> value(T data) {
		requireConfigurable(this.value);
		this.value = data;

		return getThis();
	}

	@Override
	public DataNodeConfigurator<T> value(DataSource dataSource) {
		requireConfigurable(value);
		value =

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
}
