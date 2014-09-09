package uk.co.strangeskies.modabi.schema.model.building.configurators;

import uk.co.strangeskies.modabi.schema.model.AbstractModel;
import uk.co.strangeskies.modabi.schema.model.Model;
import uk.co.strangeskies.modabi.schema.model.nodes.BindingChildNode;
import uk.co.strangeskies.modabi.schema.model.nodes.ChildNode;

public interface AbstractModelConfigurator<S extends AbstractModelConfigurator<S, N, T>, N extends AbstractModel<T, ?, ?>, T>
		extends
		BindingNodeConfigurator<S, N, T, ChildNode<?, ?>, BindingChildNode<?, ?, ?>>,
		BranchingNodeConfigurator<S, N, ChildNode<?, ?>, BindingChildNode<?, ?, ?>> {
	public <V extends T> AbstractModelConfigurator<?, ?, V> baseModel(
			@SuppressWarnings("unchecked") Model<? super V>... baseModel);

	@Override
	public <V extends T> AbstractModelConfigurator<?, ?, V> dataClass(
			Class<V> dataClass);
}
