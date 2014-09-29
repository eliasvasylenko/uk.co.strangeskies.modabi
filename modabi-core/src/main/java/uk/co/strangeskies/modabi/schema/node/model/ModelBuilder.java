package uk.co.strangeskies.modabi.schema.node.model;

import uk.co.strangeskies.modabi.schema.node.building.DataLoader;

public interface ModelBuilder {
	public ModelConfigurator<Object> configure(DataLoader loader);
}
