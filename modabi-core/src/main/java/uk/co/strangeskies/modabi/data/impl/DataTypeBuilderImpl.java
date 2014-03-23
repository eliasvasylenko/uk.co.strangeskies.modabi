package uk.co.strangeskies.modabi.data.impl;

import uk.co.strangeskies.modabi.data.DataTypeBuilder;
import uk.co.strangeskies.modabi.data.DataTypeConfigurator;

public class DataTypeBuilderImpl implements DataTypeBuilder {
	@Override
	public DataTypeConfigurator<Object> configure() {
		return new DataTypeConfiguratorImpl<Object>();
	}
}
