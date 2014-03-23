package uk.co.strangeskies.modabi.data;

import java.util.function.Function;

import uk.co.strangeskies.gears.utilities.factory.Factory;
import uk.co.strangeskies.modabi.model.building.PropertyNodeConfigurator;

public interface DataTypeRestrictionConfigurator<T> extends
		Factory<DataType<T>> {
	default DataTypeRestrictionConfigurator<T> addProperty(
			Function<PropertyNodeConfigurator<Object>, PropertyNodeConfigurator<?>> propertyConfiguration) {
		propertyConfiguration.apply(addProperty()).create();
		return this;
	}

	PropertyNodeConfigurator<Object> addProperty();
}
