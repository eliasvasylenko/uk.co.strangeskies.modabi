package uk.co.strangeskies.modabi.schema.node.model.impl;

import uk.co.strangeskies.modabi.schema.node.building.DataLoader;
import uk.co.strangeskies.modabi.schema.node.model.ModelBuilder;
import uk.co.strangeskies.modabi.schema.node.model.ModelConfigurator;

public class ModelBuilderImpl implements ModelBuilder {
	@Override
	public ModelConfigurator<Object> configure(DataLoader loader) {
		return new ModelConfiguratorImpl<>(loader);
	}
}
