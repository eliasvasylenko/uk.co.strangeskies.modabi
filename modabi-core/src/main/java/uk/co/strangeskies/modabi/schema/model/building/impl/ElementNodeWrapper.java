package uk.co.strangeskies.modabi.schema.model.building.impl;

import java.util.Collections;
import java.util.List;

import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.model.AbstractModel;
import uk.co.strangeskies.modabi.schema.model.Model;
import uk.co.strangeskies.modabi.schema.model.nodes.ElementNode;

public class ElementNodeWrapper<T>
		extends
		BindingNodeWrapper<T, AbstractModel.Effective<? super T, ?, ?>, ElementNode.Effective<? super T>, ElementNode<T>, ElementNode.Effective<T>>
		implements ElementNode.Effective<T> {
	public ElementNodeWrapper(AbstractModel.Effective<? super T, ?, ?> component) {
		super(component);
	}

	public ElementNodeWrapper(Model.Effective<T> component,
			ElementNode.Effective<? super T> base) {
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
