package uk.co.strangeskies.modabi.schema.model.building.configurators;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import uk.co.strangeskies.modabi.schema.model.Model;
import uk.co.strangeskies.modabi.schema.model.nodes.BindingChildNode;
import uk.co.strangeskies.modabi.schema.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.schema.model.nodes.ElementNode;

public interface ElementNodeConfigurator<T>
		extends
		AbstractModelConfigurator<ElementNodeConfigurator<T>, ElementNode<T>, T>,
		BindingChildNodeConfigurator<ElementNodeConfigurator<T>, ElementNode<T>, T, ChildNode<?, ?>, BindingChildNode<?, ?, ?>> {
	@Override
	default <V extends T> ElementNodeConfigurator<V> baseModel(
			@SuppressWarnings("unchecked") Model<? super V>... baseModel) {
		return baseModel(new HashSet<>(Arrays.asList(baseModel)));
	}

	@Override
	<V extends T> ElementNodeConfigurator<V> baseModel(
			Set<? extends Model<? super V>> baseModel);

	@Override
	<V extends T> ElementNodeConfigurator<V> dataClass(Class<V> dataClass);
}
