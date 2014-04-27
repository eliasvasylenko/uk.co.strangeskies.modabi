package uk.co.strangeskies.modabi.model.building;

import uk.co.strangeskies.modabi.model.AbstractModel;
import uk.co.strangeskies.modabi.model.Model;
import uk.co.strangeskies.modabi.model.nodes.ChildNode;

public interface AbstractModelConfigurator<S extends AbstractModelConfigurator<S, N, T>, N extends AbstractModel<T>, T>
		extends BindingNodeConfigurator<S, N, T>,
		BranchingNodeConfigurator<S, N, ChildBuilder, ChildNode> {
	public S isAbstract(boolean isAbstract);

	public <V extends T> AbstractModelConfigurator<?, ?, V> baseModel(
			@SuppressWarnings("unchecked") Model<? super V>... baseModel);

	@Override
	public <V extends T> AbstractModelConfigurator<?, ?, V> dataClass(
			Class<V> dataClass);
}
