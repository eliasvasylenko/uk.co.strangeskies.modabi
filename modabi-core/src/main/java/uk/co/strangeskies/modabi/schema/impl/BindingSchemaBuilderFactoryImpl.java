package uk.co.strangeskies.modabi.schema.impl;

import uk.co.strangeskies.modabi.schema.BindingSchemaBuilder;
import uk.co.strangeskies.modabi.schema.BindingSchemaBuilderFactory;

public class BindingSchemaBuilderFactoryImpl implements BindingSchemaBuilderFactory {
	@Override
	public BindingSchemaBuilder<Object> create() {
		return new BindingSchemaBuilderImpl<Object>();
	}
}
