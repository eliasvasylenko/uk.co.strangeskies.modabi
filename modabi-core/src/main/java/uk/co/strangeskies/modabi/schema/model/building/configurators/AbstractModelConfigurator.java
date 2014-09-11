package uk.co.strangeskies.modabi.schema.model.building.configurators;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import uk.co.strangeskies.modabi.schema.model.AbstractModel;
import uk.co.strangeskies.modabi.schema.model.Model;
import uk.co.strangeskies.modabi.schema.model.nodes.BindingChildNode;
import uk.co.strangeskies.modabi.schema.model.nodes.ChildNode;

public interface AbstractModelConfigurator<S extends AbstractModelConfigurator<S, N, T>, N extends AbstractModel<T, ?, ?>, T>
		extends
		BindingNodeConfigurator<S, N, T, ChildNode<?, ?>, BindingChildNode<?, ?, ?>>,
		SchemaNodeConfigurator<S, N, ChildNode<?, ?>, BindingChildNode<?, ?, ?>> {
	default <V extends T> AbstractModelConfigurator<?, ?, V> baseModel(
			@SuppressWarnings("unchecked") Model<? super V>... baseModel) {
		return this.<V> baseModel(new HashSet<>(Arrays.asList(baseModel)));
	}

	<V extends T> AbstractModelConfigurator<?, ?, V> baseModel(
			Set<? extends Model<? super V>> baseModel);

	@Override
	<V extends T> AbstractModelConfigurator<?, ?, V> dataClass(Class<V> dataClass);
}
