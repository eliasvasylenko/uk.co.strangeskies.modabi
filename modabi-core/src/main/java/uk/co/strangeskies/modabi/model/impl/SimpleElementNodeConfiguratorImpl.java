package uk.co.strangeskies.modabi.model.impl;

import uk.co.strangeskies.gears.mathematics.Range;
import uk.co.strangeskies.modabi.data.DataType;
import uk.co.strangeskies.modabi.model.SimpleElementNode;
import uk.co.strangeskies.modabi.model.build.SimpleElementNodeConfigurator;
import uk.co.strangeskies.modabi.processing.SchemaProcessingContext;

public class SimpleElementNodeConfiguratorImpl<T>
		extends
		TypedDataNodeConfiguratorImpl<SimpleElementNodeConfigurator<T>, SimpleElementNode<T>, T>
		implements SimpleElementNodeConfigurator<T>, SimpleElementNode<T> {
	private Range<Integer> occurances;

	public SimpleElementNodeConfiguratorImpl(NodeBuilderContext context) {
		super(context);
	}

	@Override
	public SimpleElementNode<T> tryCreate() {
		return this;
	}

	@Override
	public void process(SchemaProcessingContext context) {
		context.accept(this);
	}

	@SuppressWarnings("unchecked")
	@Override
	public final <V extends T> SimpleElementNodeConfigurator<V> dataClass(
			Class<V> dataClass) {
		return (SimpleElementNodeConfigurator<V>) super.dataClass(dataClass);
	}

	@SuppressWarnings("unchecked")
	@Override
	public final <U extends T> SimpleElementNodeConfigurator<U> type(
			DataType<U> type) {
		return (SimpleElementNodeConfigurator<U>) super.type(type);
	}

	@Override
	public final SimpleElementNodeConfigurator<T> occurances(
			Range<Integer> occuranceRange) {
		occurances = occuranceRange;

		return this;
	}

	@Override
	public Range<Integer> getOccurances() {
		return occurances;
	}

	@Override
	protected boolean assertReady() {
		assertHasId();
		return super.assertReady();
	}
}
