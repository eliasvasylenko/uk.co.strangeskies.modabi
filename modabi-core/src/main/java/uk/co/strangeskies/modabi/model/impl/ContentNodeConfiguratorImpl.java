package uk.co.strangeskies.modabi.model.impl;

import uk.co.strangeskies.modabi.data.DataType;
import uk.co.strangeskies.modabi.model.ContentNode;
import uk.co.strangeskies.modabi.model.building.ContentNodeConfigurator;

public class ContentNodeConfiguratorImpl<T>
		extends
		TypedDataNodeConfiguratorImpl<ContentNodeConfigurator<T>, ContentNode<T>, T>
		implements ContentNodeConfigurator<T>, ContentNode<T> {
	private boolean optional;

	public ContentNodeConfiguratorImpl(NodeBuilderContext context) {
		super(context);
	}

	@Override
	public ContentNode<T> tryCreate() {
		return this;
	}

	@Override
	public ContentNodeConfigurator<T> optional(boolean optional) {
		this.optional = optional;
		return this;
	}

	@Override
	public boolean isOptional() {
		return optional;
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

	@Override
	public void process(SchemaProcessingContext context) {
		context.accept(this);
	}
}
