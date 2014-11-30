package uk.co.strangeskies.modabi.schema.node.model.impl;

import org.osgi.service.component.annotations.Component;

import uk.co.strangeskies.modabi.schema.node.building.DataLoader;
import uk.co.strangeskies.modabi.schema.node.model.ModelBuilder;
import uk.co.strangeskies.modabi.schema.node.model.ModelConfigurator;

@Component
public class ModelBuilderImpl implements ModelBuilder {
	@Override
	public ModelConfigurator<Object> configure(DataLoader loader) {
		return new ModelConfiguratorImpl<>(loader);
	}
}
