package uk.co.strangeskies.modabi.schema.node.wrapping.impl;

import uk.co.strangeskies.modabi.schema.node.DataNode;
import uk.co.strangeskies.modabi.schema.node.type.DataBindingType;

public class DataBindingTypeWrapper<T>
		extends
		BindingNodeWrapper<T, DataNode.Effective<T>, DataBindingType.Effective<? super T>, DataBindingType<T>, DataBindingType.Effective<T>>
		implements DataBindingType.Effective<T> {
	public DataBindingTypeWrapper(DataNode.Effective<T> component) {
		super(component);
	}

	@Override
	public DataBindingType.Effective<? super T> baseType() {
		return getComponent().type();
	}

	@Override
	public Boolean isPrivate() {
		return getBase() == null ? null : getBase().isPrivate();
	}
}
