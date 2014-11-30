package uk.co.strangeskies.modabi.schema.node.wrapping.impl;

import java.util.Collections;
import java.util.List;

import uk.co.strangeskies.modabi.schema.node.AbstractComplexNode;
import uk.co.strangeskies.modabi.schema.node.model.Model;

public class ModelWrapper<T>
		extends
		BindingNodeWrapper<T, AbstractComplexNode.Effective<T, ?, ?>, Model.Effective<? super T>, Model<T>, Model.Effective<T>>
		implements Model.Effective<T> {
	public ModelWrapper(AbstractComplexNode.Effective<T, ?, ?> component) {
		super(component);
	}

	@Override
	public List<Model.Effective<? super T>> baseModel() {
		return Collections.unmodifiableList(getComponent().baseModel());
	}
}
