package uk.co.strangeskies.modabi.impl.schema.building;

import uk.co.strangeskies.modabi.schema.DataType;
import uk.co.strangeskies.modabi.schema.DataTypeConfigurator;
import uk.co.strangeskies.reflection.TypeToken;

public class DataTypeConfiguratorDecorator<T> extends
		BindingNodeConfiguratorDecorator<DataTypeConfigurator<T>, DataType<T>, T> implements DataTypeConfigurator<T> {
	public DataTypeConfiguratorDecorator(DataTypeConfigurator<T> component) {
		super(component);
	}

	@Override
	public DataType<T> create() {
		return getComponent().create();
	}

	@Override
	public DataTypeConfigurator<T> isPrivate(boolean hidden) {
		setComponent(getComponent().isPrivate(hidden));
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <U extends T> DataTypeConfigurator<U> baseType(DataType<? super U> baseType) {
		setComponent((DataTypeConfigurator<T>) getComponent().baseType(baseType));
		return (DataTypeConfigurator<U>) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V extends T> DataTypeConfigurator<V> dataType(TypeToken<? extends V> bindingClass) {
		return (DataTypeConfigurator<V>) super.dataType(bindingClass);
	}
}
