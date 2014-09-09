package uk.co.strangeskies.modabi.schema.model.building.impl;

import uk.co.strangeskies.modabi.schema.model.building.DataLoader;
import uk.co.strangeskies.modabi.schema.model.building.ModelBuilder;
import uk.co.strangeskies.modabi.schema.model.building.configurators.ModelConfigurator;
import uk.co.strangeskies.modabi.schema.model.building.configurators.impl.ModelConfiguratorImpl;

public class ModelBuilderImpl implements ModelBuilder {
	@Override
	public ModelConfigurator<Object> configure(DataLoader loader) {
		return new ModelConfiguratorImpl<>(loader);
	}
}
