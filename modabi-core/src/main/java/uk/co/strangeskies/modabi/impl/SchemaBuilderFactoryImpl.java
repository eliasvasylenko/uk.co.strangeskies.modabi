package uk.co.strangeskies.modabi.impl;

import uk.co.strangeskies.modabi.SchemaBuilder;
import uk.co.strangeskies.modabi.SchemaBuilderFactory;

public class SchemaBuilderFactoryImpl implements SchemaBuilderFactory {
	@Override
	public SchemaBuilder<Object> create() {
		return new SchemaBuilderImpl<>();
	}
}
