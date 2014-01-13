package uk.co.strangeskies.modabi.model.impl;

import uk.co.strangeskies.gears.mathematics.Range;
import uk.co.strangeskies.modabi.model.ElementNode;
import uk.co.strangeskies.modabi.model.Model;
import uk.co.strangeskies.modabi.model.build.ElementNodeConfigurator;
import uk.co.strangeskies.modabi.processing.SchemaProcessingContext;

public class ElementNodeConfiguratorImpl<T>
		extends
		AbstractModelConfiguratorImpl<ElementNodeConfigurator<T>, ElementNode<T>, T>
		implements ElementNodeConfigurator<T>, ElementNode<T> {
	private Range<Integer> occurances;
	private boolean iterable;
	private String outMethodName;
	private String inMethodName;
	private boolean inMethodChained;

	public ElementNodeConfiguratorImpl(NodeBuilderContext context) {
		super(context);
	}

	@Override
	public ElementNode<T> tryCreate() {
		return this;
	}

	@Override
	public void process(SchemaProcessingContext context) {
		context.accept(this);
	}

	@Override
	public String getOutMethod() {
		return outMethodName;
	}

	@Override
	public boolean isOutMethodIterable() {
		return iterable;
	}

	@Override
	public String getInMethod() {
		return inMethodName;
	}

	@Override
	public boolean isInMethodChained() {
		return inMethodChained;
	}

	@Override
	public Range<Integer> getOccurances() {
		return occurances;
	}

	@SuppressWarnings("unchecked")
	@Override
	public final <V extends T> ElementNodeConfigurator<V> dataClass(
			Class<V> dataClass) {
		return (ElementNodeConfigurator<V>) super.dataClass(dataClass);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V extends T> ElementNodeConfigurator<V> baseModel(
			Model<? super V>... base) {
		return (ElementNodeConfigurator<V>) super.baseModel(base);
	}

	@Override
	public ElementNodeConfigurator<T> occurances(Range<Integer> occuranceRange) {
		this.occurances = occuranceRange;

		return this;
	}

	@Override
	public ElementNodeConfigurator<T> inMethod(String inMethodName) {
		this.inMethodName = inMethodName;

		return this;
	}

	@Override
	public ElementNodeConfigurator<T> inMethodChained(boolean chained) {
		this.inMethodChained = chained;

		return this;
	}

	@Override
	public ElementNodeConfigurator<T> outMethod(String outMethodName) {
		this.outMethodName = outMethodName;

		return this;
	}

	@Override
	public ElementNodeConfigurator<T> outMethodIterable(boolean iterable) {
		this.iterable = iterable;

		return this;
	}
}
