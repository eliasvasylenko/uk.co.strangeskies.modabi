package uk.co.strangeskies.modabi.model.impl;

import java.util.Collection;

import uk.co.strangeskies.gears.mathematics.Range;
import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.data.DataType;
import uk.co.strangeskies.modabi.model.SimpleElementNode;
import uk.co.strangeskies.modabi.model.building.SimpleElementNodeConfigurator;
import uk.co.strangeskies.modabi.processing.SchemaProcessingContext;

class SimpleElementNodeConfiguratorImpl<T>
		extends
		TypedDataNodeConfiguratorImpl<SimpleElementNodeConfigurator<T>, SimpleElementNode<T>, T>
		implements SimpleElementNodeConfigurator<T> {
	protected static class SimpleElementNodeImpl<T> extends TypedDataNodeImpl<T>
			implements SimpleElementNode<T> {
		private final Range<Integer> occurances;

		SimpleElementNodeImpl(SimpleElementNodeConfiguratorImpl<T> configurator) {
			super(configurator);

			occurances = configurator.occurances;
		}

		SimpleElementNodeImpl(SimpleElementNodeImpl<T> node,
				Collection<? extends SimpleElementNodeImpl<T>> overriddenNodes) {
			super(node, overriddenNodes);

			occurances = getValue(node, overriddenNodes, n -> n.getOccurances(), (v,
					o) -> o.contains(v));
		}

		@Override
		protected void validateAsEffectiveModel(boolean isAbstract) {
			super.validateAsEffectiveModel(isAbstract);

			if (occurances == null)
				throw new SchemaException();
		}

		@Override
		public Range<Integer> getOccurances() {
			return occurances;
		}

		@Override
		public <U> U process(SchemaProcessingContext<U> context) {
			return context.accept(this);
		}
	}

	private Range<Integer> occurances;

	public SimpleElementNodeConfiguratorImpl(
			BranchingNodeConfiguratorImpl<?, ?> parent) {
		super(parent);
	}

	@Override
	public SimpleElementNode<T> tryCreate() {
		return new SimpleElementNodeImpl<>(this);
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
	public Class<SimpleElementNode<T>> getNodeClass() {
		// TODO Auto-generated method stub
		return null;
	}
}
