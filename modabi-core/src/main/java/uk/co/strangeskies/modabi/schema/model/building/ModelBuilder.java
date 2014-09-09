package uk.co.strangeskies.modabi.schema.model.building;

import uk.co.strangeskies.modabi.schema.model.building.configurators.ModelConfigurator;

public interface ModelBuilder {
	public ModelConfigurator<Object> configure(DataLoader loader);
}
