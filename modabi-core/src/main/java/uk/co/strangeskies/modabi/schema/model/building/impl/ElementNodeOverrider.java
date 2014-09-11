package uk.co.strangeskies.modabi.schema.model.building.impl;

import java.util.Collections;
import java.util.Set;

import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.model.Model;
import uk.co.strangeskies.modabi.schema.model.building.DataLoader;
import uk.co.strangeskies.modabi.schema.model.building.ModelBuilder;
import uk.co.strangeskies.modabi.schema.model.nodes.ElementNode;

public class ElementNodeOverrider<T> implements ElementNode.Effective<T> {
	public ElementNodeOverrider(ElementNode.Effective<? super T> element,
			Model.Effective<T> override, ModelBuilder builder, DataLoader loader) {

	}

	public ElementNodeOverrider(Model.Effective<T> component,
			ElementNode.Effective<? super T> base) {
		super(component, base);

		String message = "Cannot override '" + base.getName() + "' with '"
				+ component.getName() + "'.";

		if (!component.baseModel().containsAll(base.baseModel()))
			throw new SchemaException(message);
	}

	@Override
	public Set<Model.Effective<? super T>> baseModel() {
		return Collections.unmodifiableSet(getComponent().baseModel());
	}
}
