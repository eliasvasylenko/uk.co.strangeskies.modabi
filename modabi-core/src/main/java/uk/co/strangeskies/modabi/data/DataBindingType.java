package uk.co.strangeskies.modabi.data;

import uk.co.strangeskies.modabi.model.nodes.BindingNode;

public interface DataBindingType<T> extends
		BindingNode<T, DataBindingType.Effective<T>> {
	interface Effective<T> extends DataBindingType<T>,
			BindingNode.Effective<T, Effective<T>> {
		@Override
		DataBindingType<T> source();
	}

	default DataBindingType<T> source() {
		return this;
	}

	Boolean isAbstract();

	Boolean isPrivate();

	DataBindingType<? super T> baseType();
}
