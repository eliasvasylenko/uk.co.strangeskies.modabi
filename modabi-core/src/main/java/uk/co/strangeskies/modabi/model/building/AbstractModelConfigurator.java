package uk.co.strangeskies.modabi.model.building;

import uk.co.strangeskies.modabi.model.ImplementationStrategy;
import uk.co.strangeskies.modabi.model.Model;

public interface AbstractModelConfigurator<S extends AbstractModelConfigurator<S, N, T>, N extends Model<T>, T>
		extends BranchingNodeConfigurator<S, N> {
	public S isAbstract(boolean isAbstract);

	public <V extends T> AbstractModelConfigurator<?, ?, V> baseModel(
			@SuppressWarnings("unchecked") Model<? super V>... baseModel);

	public S adaptionModel(Model<?> adaptionModel);

	public <V extends T> AbstractModelConfigurator<?, ?, V> dataClass(
			Class<V> dataClass);

	public S implementationStrategy(ImplementationStrategy bindingStrategy);

	public S builderClass(Class<?> factoryClass);

	public S builderMethod(String buildMethodName);
}
