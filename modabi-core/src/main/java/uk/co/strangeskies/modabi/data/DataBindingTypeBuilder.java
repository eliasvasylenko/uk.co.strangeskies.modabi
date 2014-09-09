package uk.co.strangeskies.modabi.data;

import uk.co.strangeskies.modabi.schema.model.building.DataLoader;

public interface DataBindingTypeBuilder {
	public DataBindingTypeConfigurator<Object> configure(DataLoader loader);
}
