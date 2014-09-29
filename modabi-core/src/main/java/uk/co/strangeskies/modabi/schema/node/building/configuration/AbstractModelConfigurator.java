package uk.co.strangeskies.modabi.schema.node.building.configuration;

import java.util.Arrays;
import java.util.List;

import uk.co.strangeskies.modabi.schema.node.AbstractModel;
import uk.co.strangeskies.modabi.schema.node.BindingChildNode;
import uk.co.strangeskies.modabi.schema.node.ChildNode;
import uk.co.strangeskies.modabi.schema.node.model.Model;

public interface AbstractModelConfigurator<S extends AbstractModelConfigurator<S, N, T>, N extends AbstractModel<T, ?, ?>, T>
		extends
		BindingNodeConfigurator<S, N, T, ChildNode<?, ?>, BindingChildNode<?, ?, ?>>,
		SchemaNodeConfigurator<S, N, ChildNode<?, ?>, BindingChildNode<?, ?, ?>> {
	default <V extends T> AbstractModelConfigurator<?, ?, V> baseModel(
			@SuppressWarnings("unchecked") Model<? super V>... baseModel) {
		return this.<V> baseModel(Arrays.asList(baseModel));
	}

	<V extends T> AbstractModelConfigurator<?, ?, V> baseModel(
			List<? extends Model<? super V>> baseModel);

	@Override
	<V extends T> AbstractModelConfigurator<?, ?, V> dataClass(Class<V> dataClass);
}
