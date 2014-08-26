package uk.co.strangeskies.modabi.model.building.configurators;

import uk.co.strangeskies.modabi.model.Model;
import uk.co.strangeskies.modabi.model.nodes.BindingChildNode;
import uk.co.strangeskies.modabi.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.model.nodes.ElementNode;

public interface ElementNodeConfigurator<T>
		extends
		AbstractModelConfigurator<ElementNodeConfigurator<T>, ElementNode<T>, T>,
		BindingChildNodeConfigurator<ElementNodeConfigurator<T>, ElementNode<T>, T, ChildNode<?, ?>, BindingChildNode<?, ?, ?>> {
	@Override
	public <V extends T> ElementNodeConfigurator<V> baseModel(
			@SuppressWarnings("unchecked") Model<? super V>... baseModel);

	@Override
	public <V extends T> ElementNodeConfigurator<V> dataClass(Class<V> dataClass);
}
