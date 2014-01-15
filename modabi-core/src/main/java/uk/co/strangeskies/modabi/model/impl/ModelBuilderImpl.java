package uk.co.strangeskies.modabi.model.impl;

import uk.co.strangeskies.modabi.model.building.ModelBuilder;
import uk.co.strangeskies.modabi.model.building.ModelConfigurator;

public class ModelBuilderImpl implements ModelBuilder {
	@Override
	public ModelConfigurator<Object> configure() {
		return new ModelConfiguratorImpl<>();
	}
}
