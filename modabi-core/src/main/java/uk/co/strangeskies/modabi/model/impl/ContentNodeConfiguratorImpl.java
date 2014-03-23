package uk.co.strangeskies.modabi.model.impl;

import java.util.Collection;

import uk.co.strangeskies.modabi.data.DataType;
import uk.co.strangeskies.modabi.model.building.ContentNodeConfigurator;
import uk.co.strangeskies.modabi.model.nodes.ContentNode;
import uk.co.strangeskies.modabi.processing.SchemaProcessingContext;
import uk.co.strangeskies.modabi.processing.SchemaResultProcessingContext;

public class ContentNodeConfiguratorImpl<T>
		extends
		TypedDataNodeConfiguratorImpl<ContentNodeConfigurator<T>, ContentNode<T>, T>
		implements ContentNodeConfigurator<T> {
	protected static class ContentNodeImpl<T> extends TypedDataNodeImpl<T> implements
			ContentNode<T> {
		private final Boolean optional;

		public ContentNodeImpl(ContentNodeConfiguratorImpl<T> configurator) {
			super(configurator);

			optional = configurator.optional;
		}

		public ContentNodeImpl(ContentNode<T> node,
				Collection<? extends ContentNode<T>> overriddenNodes,
				Class<?> parentClass) {
			super(node, overriddenNodes, parentClass);

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

		@Override
		public <U> U process(SchemaResultProcessingContext<U> context) {
			return context.accept(this);
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

	@Override
	protected ContentNode<T> getEffective(ContentNode<T> node) {
		return new ContentNodeImpl<>(node, getOverriddenNodes(), getParentClass());
	}
}
