package uk.co.strangeskies.modabi.model.impl;

import java.util.Collection;

import uk.co.strangeskies.modabi.data.DataType;
import uk.co.strangeskies.modabi.model.PropertyNode;
import uk.co.strangeskies.modabi.model.building.PropertyNodeConfigurator;
import uk.co.strangeskies.modabi.processing.SchemaProcessingContext;

class PropertyNodeConfiguratorImpl<T>
		extends
		TypedDataNodeConfiguratorImpl<PropertyNodeConfigurator<T>, PropertyNode<T>, T>
		implements PropertyNodeConfigurator<T> {
	protected static class PropertyNodeImpl<T> extends TypedDataNodeImpl<T>
			implements PropertyNode<T> {
		private final Boolean optional;

		public PropertyNodeImpl(PropertyNodeConfiguratorImpl<T> configurator) {
			super(configurator);

			optional = configurator.optional;
		}

		private PropertyNodeImpl(PropertyNode<T> node,
				Collection<? extends PropertyNode<T>> overriddenNodes) {
			super(node, overriddenNodes);

			optional = getValue(node, overriddenNodes, n -> n.isOptional(),
					(v, o) -> !v || o);
		}

		@Override
		public Boolean isOptional() {
			return optional;
		}

		@Override
		public void process(SchemaProcessingContext context) {
			context.accept(this);
		}
	}

	private boolean optional;

	public PropertyNodeConfiguratorImpl(BranchingNodeConfiguratorImpl<?, ?> parent) {
		super(parent);
	}

	@Override
	public PropertyNode<T> tryCreate() {
		return new PropertyNodeImpl<>(this);
	}

	@Override
	public PropertyNodeConfigurator<T> optional(boolean optional) {
		this.optional = optional;
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V extends T> PropertyNodeConfigurator<V> dataClass(Class<V> dataClass) {
		return (PropertyNodeConfigurator<V>) super.dataClass(dataClass);
	}

	@SuppressWarnings("unchecked")
	@Override
	public final <U extends T> PropertyNodeConfigurator<U> type(DataType<U> type) {
		return (PropertyNodeConfigurator<U>) super.type(type);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<PropertyNode<T>> getNodeClass() {
		return (Class<PropertyNode<T>>) (Object) PropertyNode.class;
	}
}
