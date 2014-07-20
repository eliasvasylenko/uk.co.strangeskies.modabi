package uk.co.strangeskies.modabi.model.building.impl;

import uk.co.strangeskies.modabi.model.building.DataLoader;
import uk.co.strangeskies.modabi.model.building.ModelBuilder;
import uk.co.strangeskies.modabi.model.building.configurators.ModelConfigurator;
import uk.co.strangeskies.modabi.model.building.impl.configurators.ModelConfiguratorImpl;

public class ModelBuilderImpl implements ModelBuilder {
	@Override
	public ModelConfigurator<Object> configure(DataLoader loader) {
		return new ModelConfiguratorImpl<>(loader);
	}
}
