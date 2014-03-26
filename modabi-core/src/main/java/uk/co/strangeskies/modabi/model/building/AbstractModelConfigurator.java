package uk.co.strangeskies.modabi.model.building;

import uk.co.strangeskies.modabi.model.AbstractModel;
import uk.co.strangeskies.modabi.model.Model;
import uk.co.strangeskies.modabi.processing.BindingStrategy;
import uk.co.strangeskies.modabi.processing.UnbindingStrategy;

public interface AbstractModelConfigurator<S extends AbstractModelConfigurator<S, N, T>, N extends AbstractModel<T>, T>
		extends BranchingNodeConfigurator<S, N> {
	public S isAbstract(boolean isAbstract);

	public <V extends T> AbstractModelConfigurator<?, ?, V> baseModel(
			@SuppressWarnings("unchecked") Model<? super V>... baseModel);

	public <V extends T> AbstractModelConfigurator<?, ?, V> dataClass(
			Class<V> dataClass);

	public S bindingStrategy(BindingStrategy strategy);

	public S unbindingStrategy(UnbindingStrategy strategy);

	public S bindingClass(Class<?> bindingClass);

	public S unbindingClass(Class<?> unbindingClass);
}
