package uk.co.strangeskies.modabi.data.impl;

import uk.co.strangeskies.modabi.data.DataBindingTypeBuilder;
import uk.co.strangeskies.modabi.data.DataBindingTypeConfigurator;
import uk.co.strangeskies.modabi.schema.model.building.DataLoader;

public class DataBindingTypeBuilderImpl implements DataBindingTypeBuilder {
	@Override
	public DataBindingTypeConfigurator<Object> configure(DataLoader loader) {
		return new DataBindingTypeConfiguratorImpl<Object>(loader);
	}
}
