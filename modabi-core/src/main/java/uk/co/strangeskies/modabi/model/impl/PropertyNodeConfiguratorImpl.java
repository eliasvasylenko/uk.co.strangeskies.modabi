package uk.co.strangeskies.modabi.model.impl;

import uk.co.strangeskies.modabi.data.DataType;
import uk.co.strangeskies.modabi.model.PropertyNode;
import uk.co.strangeskies.modabi.model.building.PropertyNodeConfigurator;
import uk.co.strangeskies.modabi.processing.SchemaProcessingContext;

public class PropertyNodeConfiguratorImpl<T>
		extends
		TypedDataNodeConfiguratorImpl<PropertyNodeConfigurator<T>, PropertyNode<T>, T>
		implements PropertyNodeConfigurator<T>, PropertyNode<T> {
	private boolean optional;

	public PropertyNodeConfiguratorImpl(NodeBuilderContext context) {
		super(context);
	}

	@Override
	public PropertyNode<T> tryCreate() {
		return this;
	}

	@Override
	public void process(SchemaProcessingContext context) {
		context.accept(this);
	}

	@Override
	public PropertyNodeConfigurator<T> optional(boolean optional) {
		this.optional = optional;
		return this;
	}

	@Override
	public boolean isOptional() {
		return optional;
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

	@Override
	protected boolean assertReady() {
		assertHasId();
		return super.assertReady();
	}
}
