package uk.co.strangeskies.modabi.model.impl;

import uk.co.strangeskies.modabi.data.DataType;
import uk.co.strangeskies.modabi.model.PropertyNode;
import uk.co.strangeskies.modabi.model.building.PropertyNodeConfigurator;
import uk.co.strangeskies.modabi.processing.SchemaProcessingContext;

class PropertyNodeConfiguratorImpl<T>
		extends
		TypedDataNodeConfiguratorImpl<PropertyNodeConfigurator<T>, PropertyNode<T>, T>
		implements PropertyNodeConfigurator<T> {
	protected static class PropertyNodeImpl<T> extends
			EffectivePropertyNodeImpl<T> {
		private final EffectivePropertyNodeImpl<T> effectiveModel;

		PropertyNodeImpl(PropertyNodeConfiguratorImpl<T> configurator) {
			super(configurator);

			PropertyNode<T> overriddenNode = configurator.getOverriddenNode();
			effectiveModel = overriddenNode == null ? this
					: new EffectivePropertyNodeImpl<>(this, overriddenNode);
			effectiveModel.validateEffectiveModel();
		}

		@Override
		public EffectivePropertyNodeImpl<T> effectiveModel() {
			return effectiveModel;
		}
	}

	protected static class EffectivePropertyNodeImpl<T> extends
			TypedDataNodeImpl<T> implements PropertyNode<T> {
		private final Boolean optional;

		public EffectivePropertyNodeImpl(
				PropertyNodeConfiguratorImpl<T> configurator) {
			super(configurator);

			optional = configurator.optional;
		}

		public EffectivePropertyNodeImpl(EffectivePropertyNodeImpl<T> node,
				PropertyNode<T> overriddenNode) {
			super(node, overriddenNode);

			Boolean overriddenOptional = overriddenNode.isOptional();
			if (node.optional != null) {
				optional = node.optional;
				if (overriddenOptional != null && !overriddenOptional && optional)
					throw new SchemaException();
			} else
				optional = overriddenOptional;
		}

		@Override
		public Boolean isOptional() {
			return optional;
		}

		@Override
		public void process(SchemaProcessingContext context) {
			context.accept(this);
		}

		@Override
		protected SchemaNodeConfiguratorImpl.SchemaNodeImpl effectiveModel() {
			return this;
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
