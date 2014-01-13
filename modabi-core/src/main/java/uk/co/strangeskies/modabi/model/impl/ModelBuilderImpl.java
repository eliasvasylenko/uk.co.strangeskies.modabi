package uk.co.strangeskies.modabi.model.impl;

import uk.co.strangeskies.modabi.model.build.ModelBuilder;
import uk.co.strangeskies.modabi.model.build.ModelConfigurator;

public class ModelBuilderImpl implements ModelBuilder {
	public ModelConfigurator<Object> configure() {
		return new ModelConfiguratorImpl<>();
	}
}
