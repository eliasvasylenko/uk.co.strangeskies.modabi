package uk.co.strangeskies.modabi.data;

import uk.co.strangeskies.modabi.namespace.QualifiedNamedSet;

public class DataBindingTypes extends QualifiedNamedSet<DataBindingType<?>> {
	public DataBindingTypes() {
		super(DataBindingType::getName);
	}
}
