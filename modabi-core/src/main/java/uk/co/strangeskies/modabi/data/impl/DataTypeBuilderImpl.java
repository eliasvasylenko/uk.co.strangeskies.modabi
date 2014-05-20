package uk.co.strangeskies.modabi.data.impl;

import uk.co.strangeskies.modabi.data.DataBindingTypeBuilder;
import uk.co.strangeskies.modabi.data.DataBindingTypeConfigurator;

public class DataTypeBuilderImpl implements DataBindingTypeBuilder {
	@Override
	public DataBindingTypeConfigurator<Object> configure() {
		return new DataTypeConfiguratorImpl<Object>();
	}
}
