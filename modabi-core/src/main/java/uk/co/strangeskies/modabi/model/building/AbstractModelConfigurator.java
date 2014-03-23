package uk.co.strangeskies.modabi.model.building;

import uk.co.strangeskies.modabi.model.AbstractModel;
import uk.co.strangeskies.modabi.model.Model;
import uk.co.strangeskies.modabi.processing.BindingStrategy;

public interface AbstractModelConfigurator<S extends AbstractModelConfigurator<S, N, T>, N extends AbstractModel<T>, T>
		extends BranchingNodeConfigurator<S, N> {
	public S isAbstract(boolean isAbstract);

	public <V extends T> AbstractModelConfigurator<?, ?, V> baseModel(
			@SuppressWarnings("unchecked") Model<? super V>... baseModel);

	public <V extends T> AbstractModelConfigurator<?, ?, V> dataClass(
			Class<V> dataClass);

	public S implementationStrategy(BindingStrategy bindingStrategy);

	public S builderClass(Class<?> factoryClass);
}
