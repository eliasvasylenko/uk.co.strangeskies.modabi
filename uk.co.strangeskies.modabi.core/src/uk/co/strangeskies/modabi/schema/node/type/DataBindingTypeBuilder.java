package uk.co.strangeskies.modabi.schema.node.type;

import uk.co.strangeskies.modabi.schema.node.building.DataLoader;

public interface DataBindingTypeBuilder {
	public DataBindingTypeConfigurator<Object> configure(DataLoader loader);
}
