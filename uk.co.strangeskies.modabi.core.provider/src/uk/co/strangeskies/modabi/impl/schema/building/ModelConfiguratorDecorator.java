package uk.co.strangeskies.modabi.impl.schema.building;

import java.util.List;

import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.ModelConfigurator;
import uk.co.strangeskies.reflection.TypeToken;

public class ModelConfiguratorDecorator<T> extends BindingNodeConfiguratorDecorator<ModelConfigurator<T>, Model<T>, T>
		implements ModelConfigurator<T> {
	public ModelConfiguratorDecorator(ModelConfigurator<T> component) {
		super(component);
	}

	@Override
	public Model<T> create() {
		return getComponent().create();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V extends T> ModelConfigurator<V> baseModel(List<? extends Model<? super V>> baseModel) {
		setComponent((ModelConfigurator<T>) getComponent().baseModel(baseModel));
		return (ModelConfigurator<V>) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V extends T> ModelConfigurator<V> dataType(TypeToken<? extends V> bindingClass) {
		return (ModelConfigurator<V>) super.dataType(bindingClass);
	}
}
