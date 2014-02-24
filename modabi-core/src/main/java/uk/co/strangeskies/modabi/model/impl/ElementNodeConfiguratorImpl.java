package uk.co.strangeskies.modabi.model.impl;

import java.lang.reflect.Method;

import uk.co.strangeskies.gears.mathematics.Range;
import uk.co.strangeskies.modabi.model.ElementNode;
import uk.co.strangeskies.modabi.model.Model;
import uk.co.strangeskies.modabi.model.building.ElementNodeConfigurator;
import uk.co.strangeskies.modabi.processing.SchemaProcessingContext;

class ElementNodeConfiguratorImpl<T>
		extends
		AbstractModelConfiguratorImpl<ElementNodeConfigurator<T>, ElementNode<T>, T>
		implements ElementNodeConfigurator<T> {
	protected static class ElementNodeImpl<T> extends AbstractModelImpl<T>
			implements ElementNode<T> {
		private final Range<Integer> occurances;
		private final boolean iterable;
		private final String outMethodName;
		private final Method outMethod;
		private final String inMethodName;
		private final Method inMethod;
		private final boolean inMethodChained;

		public ElementNodeImpl(ElementNodeConfiguratorImpl<T> configurator) {
			super(configurator);

			occurances = configurator.occurances;
			iterable = configurator.iterable;
			outMethodName = configurator.outMethodName;
			outMethod = ;
			inMethodName = configurator.inMethodName;
			inMethod = ;
			inMethodChained = configurator.inMethodChained;
		}

		@Override
		public Range<Integer> getOccurances() {
			return occurances;
		}

		@Override
		public String getOutMethodName() {
			return outMethodName;
		}

		@Override
		public Boolean isOutMethodIterable() {
			return iterable;
		}

		@Override
		public String getInMethodName() {
			return inMethodName;
		}

		@Override
		public Boolean isInMethodChained() {
			return inMethodChained;
		}

		@Override
		public <U> U process(SchemaProcessingContext<U> context) {
			return context.accept(this);
		}

		@Override
		protected void validateAsEffectiveModel(boolean isAbstract) {
			super.validateAsEffectiveModel(isAbstract);
		}

		@Override
		public Method getOutMethod() {
			return outMethod;
		}

		@Override
		public Method getInMethod() {
			return inMethod;
		}
	}

	private Range<Integer> occurances;
	private boolean iterable;
	private String outMethodName;
	private String inMethodName;
	private boolean inMethodChained;

	public ElementNodeConfiguratorImpl(BranchingNodeConfiguratorImpl<?, ?> parent) {
		super(parent);
	}

	@Override
	public ElementNode<T> tryCreate() {
		return new ElementNodeImpl<>(this);
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

	@SuppressWarnings("unchecked")
	@Override
	public Class<ElementNode<T>> getNodeClass() {
		return (Class<ElementNode<T>>) (Object) ElementNode.class;
	}
}
