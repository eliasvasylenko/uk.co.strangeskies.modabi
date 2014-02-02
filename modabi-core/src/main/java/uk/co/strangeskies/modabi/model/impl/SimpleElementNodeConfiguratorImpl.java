package uk.co.strangeskies.modabi.model.impl;

import uk.co.strangeskies.gears.mathematics.Range;
import uk.co.strangeskies.modabi.data.DataType;
import uk.co.strangeskies.modabi.model.SimpleElementNode;
import uk.co.strangeskies.modabi.model.building.SimpleElementNodeConfigurator;
import uk.co.strangeskies.modabi.processing.SchemaProcessingContext;

class SimpleElementNodeConfiguratorImpl<T>
		extends
		TypedDataNodeConfiguratorImpl<SimpleElementNodeConfigurator<T>, SimpleElementNode<T>, T>
		implements SimpleElementNodeConfigurator<T> {
	protected static class SimpleElementNodeImpl<T> extends
			EffectiveSimpleElementNodeImpl<T> {
		private final EffectiveSimpleElementNodeImpl<T> effectiveModel;

		SimpleElementNodeImpl(SimpleElementNodeConfiguratorImpl<T> configurator) {
			super(configurator);

			SimpleElementNode<T> overriddenNode = configurator.getOverriddenNode();
			effectiveModel = overriddenNode == null ? this
					: new EffectiveSimpleElementNodeImpl<>(this, overriddenNode);
			effectiveModel.validateEffectiveModel();
		}

		@Override
		public EffectiveSimpleElementNodeImpl<T> effectiveModel() {
			return effectiveModel;
		}
	}

	protected static class EffectiveSimpleElementNodeImpl<T> extends
			TypedDataNodeImpl<T> implements SimpleElementNode<T> {
		private final Range<Integer> occurances;

		EffectiveSimpleElementNodeImpl(
				SimpleElementNodeConfiguratorImpl<T> configurator) {
			super(configurator);

			occurances = configurator.occurances;
		}

		EffectiveSimpleElementNodeImpl(EffectiveSimpleElementNodeImpl<T> node,
				SimpleElementNode<T> overriddenNode) {
			super(node, overriddenNode);

			Range<Integer> overriddenOccurances = overriddenNode.getOccurances();
			if (node.occurances != null) {
				occurances = node.occurances;
				if (overriddenOccurances != null
						&& !overriddenOccurances.contains(occurances))
					throw new SchemaException();
			} else
				occurances = overriddenOccurances;
		}

		@Override
		protected void validateEffectiveModel() {
			super.validateEffectiveModel();
			if (occurances == null)
				throw new SchemaException();
		}

		@Override
		public Range<Integer> getOccurances() {
			return occurances;
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
