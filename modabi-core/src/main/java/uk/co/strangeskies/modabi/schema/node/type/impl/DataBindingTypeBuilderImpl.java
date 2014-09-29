package uk.co.strangeskies.modabi.schema.node.type.impl;

import uk.co.strangeskies.modabi.schema.node.building.DataLoader;
import uk.co.strangeskies.modabi.schema.node.type.DataBindingTypeBuilder;
import uk.co.strangeskies.modabi.schema.node.type.DataBindingTypeConfigurator;

public class DataBindingTypeBuilderImpl implements DataBindingTypeBuilder {
	@Override
	public DataBindingTypeConfigurator<Object> configure(DataLoader loader) {
		return new DataBindingTypeConfiguratorImpl<Object>(loader);
	}
}
