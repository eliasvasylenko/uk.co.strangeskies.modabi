package uk.co.strangeskies.modabi.model.impl;

import uk.co.strangeskies.modabi.data.DataType;
import uk.co.strangeskies.modabi.model.ContentNode;
import uk.co.strangeskies.modabi.model.building.ContentNodeConfigurator;
import uk.co.strangeskies.modabi.processing.SchemaProcessingContext;

class ContentNodeConfiguratorImpl<T>
		extends
		TypedDataNodeConfiguratorImpl<ContentNodeConfigurator<T>, ContentNode<T>, T>
		implements ContentNodeConfigurator<T> {
	protected static class ContentNodeImpl<T> extends EffectiveContentNodeImpl<T> {
		private final EffectiveContentNodeImpl<T> effectiveModel;

		ContentNodeImpl(ContentNodeConfiguratorImpl<T> configurator) {
			super(configurator);

			ContentNode<T> overriddenNode = configurator.getOverriddenNode();
			effectiveModel = overriddenNode == null ? this
					: new EffectiveContentNodeImpl<>(this, overriddenNode);
			effectiveModel.validateEffectiveModel();
		}

		@Override
		public EffectiveContentNodeImpl<T> effectiveModel() {
			return effectiveModel;
		}
	}

	protected static class EffectiveContentNodeImpl<T> extends
			TypedDataNodeImpl<T> implements ContentNode<T> {
		private final Boolean optional;

		public EffectiveContentNodeImpl(ContentNodeConfiguratorImpl<T> configurator) {
			super(configurator);

			optional = configurator.optional;
		}

		public EffectiveContentNodeImpl(EffectiveContentNodeImpl<T> node,
				ContentNode<T> overriddenNode) {
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
		protected SchemaNodeImpl effectiveModel() {
			return this;
		}
	}

	private boolean optional;

	public ContentNodeConfiguratorImpl(BranchingNodeConfiguratorImpl<?, ?> parent) {
		super(parent);
	}

	@Override
	public ContentNode<T> tryCreate() {
		return new ContentNodeImpl<>(this);
	}

	@Override
	public ContentNodeConfigurator<T> optional(boolean optional) {
		this.optional = optional;
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V extends T> ContentNodeConfigurator<V> dataClass(Class<V> dataClass) {
		return (ContentNodeConfigurator<V>) super.dataClass(dataClass);
	}

	@SuppressWarnings("unchecked")
	@Override
	public final <U extends T> ContentNodeConfigurator<U> type(DataType<U> type) {
		return (ContentNodeConfigurator<U>) super.type(type);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<ContentNode<T>> getNodeClass() {
		return (Class<ContentNode<T>>) (Object) ContentNode.class;
	}
}
