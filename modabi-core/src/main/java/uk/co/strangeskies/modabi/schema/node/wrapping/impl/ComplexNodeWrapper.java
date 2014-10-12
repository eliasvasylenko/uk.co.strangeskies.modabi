package uk.co.strangeskies.modabi.schema.node.wrapping.impl;

import java.util.Collections;
import java.util.List;

import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.node.AbstractComplexNode;
import uk.co.strangeskies.modabi.schema.node.ComplexNode;
import uk.co.strangeskies.modabi.schema.node.model.Model;

public class ComplexNodeWrapper<T>
		extends
		BindingChildNodeWrapper<T, AbstractComplexNode.Effective<? super T, ?, ?>, ComplexNode.Effective<? super T>, ComplexNode<T>, ComplexNode.Effective<T>>
		implements ComplexNode.Effective<T> {
	public ComplexNodeWrapper(
			AbstractComplexNode.Effective<? super T, ?, ?> component) {
		super(component);
	}

	public ComplexNodeWrapper(Model.Effective<T> component,
			ComplexNode.Effective<? super T> base) {
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

	@Override
	public Boolean isInline() {
		return getBase() == null ? null : getBase().isInline();
	}
}
