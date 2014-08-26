package uk.co.strangeskies.modabi.data;

import uk.co.strangeskies.modabi.model.nodes.BindingNode;
import uk.co.strangeskies.utilities.PropertySet;

public interface DataBindingType<T> extends
		BindingNode<T, DataBindingType<T>, DataBindingType.Effective<T>> {
	interface Effective<T> extends DataBindingType<T>,
			BindingNode.Effective<T, DataBindingType<T>, Effective<T>> {
		@Override
		DataBindingType<T> source();
	}

	@Override
	default DataBindingType<T> source() {
		return this;
	}

	Boolean isPrivate();

	DataBindingType<? super T> baseType();

	@Override
	default PropertySet<DataBindingType<T>> propertySet() {
		return BindingNode.super.propertySet().add(DataBindingType::isPrivate)
				.add(DataBindingType::baseType);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	default Class<Effective<T>> getEffectiveClass() {
		return (Class) Effective.class;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	default Class<DataBindingType<T>> getNodeClass() {
		return (Class) DataBindingType.class;
	}
}
