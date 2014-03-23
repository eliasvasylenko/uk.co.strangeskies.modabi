package uk.co.strangeskies.modabi.model.impl;

import java.util.Collection;

import uk.co.strangeskies.gears.mathematics.Range;
import uk.co.strangeskies.modabi.data.DataType;
import uk.co.strangeskies.modabi.model.building.SimpleElementNodeConfigurator;
import uk.co.strangeskies.modabi.model.nodes.SimpleElementNode;
import uk.co.strangeskies.modabi.processing.SchemaProcessingContext;
import uk.co.strangeskies.modabi.processing.SchemaResultProcessingContext;

public class SimpleElementNodeConfiguratorImpl<T>
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

		SimpleElementNodeImpl(SimpleElementNode<T> node,
				Collection<? extends SimpleElementNode<T>> overriddenNodes,
				Class<?> parentClass) {
			super(node, overriddenNodes, parentClass);

			occurances = getValue(node, overriddenNodes, n -> n.getOccurances(), (v,
					o) -> o.contains(v));
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
		public <U> U process(SchemaResultProcessingContext<U> context) {
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

	@Override
	protected SimpleElementNode<T> getEffective(SimpleElementNode<T> node) {
		return new SimpleElementNodeImpl<>(node, getOverriddenNodes(),
				getParentClass());
	}
}
