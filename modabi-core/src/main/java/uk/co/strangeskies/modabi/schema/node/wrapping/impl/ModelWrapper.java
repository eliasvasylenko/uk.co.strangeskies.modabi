package uk.co.strangeskies.modabi.schema.node.wrapping.impl;

import java.util.Collections;
import java.util.List;

import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.node.AbstractModel;
import uk.co.strangeskies.modabi.schema.node.ElementNode;
import uk.co.strangeskies.modabi.schema.node.model.Model;

public class ModelWrapper<T>
		extends
		BindingNodeWrapper<T, AbstractModel.Effective<? super T, ?, ?>, Model.Effective<? super T>, Model<T>, Model.Effective<T>>
		implements Model.Effective<T> {
	public ModelWrapper(AbstractModel.Effective<? super T, ?, ?> component) {
		super(component);
	}

	public ModelWrapper(ElementNode.Effective<T> component,
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
