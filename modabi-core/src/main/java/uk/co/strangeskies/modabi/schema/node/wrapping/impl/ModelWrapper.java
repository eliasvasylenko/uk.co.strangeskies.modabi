package uk.co.strangeskies.modabi.schema.node.wrapping.impl;

import java.util.Collections;
import java.util.List;

import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.node.AbstractComplexNode;
import uk.co.strangeskies.modabi.schema.node.ComplexNode;
import uk.co.strangeskies.modabi.schema.node.model.Model;

public class ModelWrapper<T>
		extends
		BindingNodeWrapper<T, AbstractComplexNode.Effective<? super T, ?, ?>, Model.Effective<? super T>, Model<T>, Model.Effective<T>>
		implements Model.Effective<T> {
	public ModelWrapper(AbstractComplexNode.Effective<? super T, ?, ?> component) {
		super(component);
	}

	public ModelWrapper(ComplexNode.Effective<T> component,
			Model.Effective<? super T> base) {
		super(component, base);

		String message = "Cannot override '" + base.getName() + "' with '"
				+ component.getName() + "'.";

		if (!component.baseModel().containsAll(base.baseModel()))
			throw new SchemaException(message);
	}

	@Override
	public List<Model.Effective<? super T>> baseModel() {
		return Collections.unmodifiableList(getComponent().baseModel());
	}
}
