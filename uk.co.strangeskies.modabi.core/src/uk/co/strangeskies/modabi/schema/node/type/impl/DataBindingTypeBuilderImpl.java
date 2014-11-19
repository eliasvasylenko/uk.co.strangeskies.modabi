package uk.co.strangeskies.modabi.schema.node.type.impl;

import org.osgi.service.component.annotations.Component;

import uk.co.strangeskies.modabi.schema.node.building.DataLoader;
import uk.co.strangeskies.modabi.schema.node.type.DataBindingTypeBuilder;
import uk.co.strangeskies.modabi.schema.node.type.DataBindingTypeConfigurator;

@Component
public class DataBindingTypeBuilderImpl implements DataBindingTypeBuilder {
	@Override
	public DataBindingTypeConfigurator<Object> configure(DataLoader loader) {
		return new DataBindingTypeConfiguratorImpl<Object>(loader);
	}
}
