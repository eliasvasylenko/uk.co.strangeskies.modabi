package uk.co.strangeskies.modabi.model.building;

import uk.co.strangeskies.modabi.model.building.configurators.ModelConfigurator;

public interface ModelBuilder {
	public ModelConfigurator<Object> configure(DataLoader loader);
}
