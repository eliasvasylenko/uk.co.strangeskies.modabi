package uk.co.strangeskies.modabi.impl.schema;

import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.schema.Model;

public class ModelImpl<T> extends BindingPointImpl<T> implements Model<T> {
	private final Schema schema;

	protected ModelImpl(ModelConfiguratorImpl<T> configurator) {
		super(configurator);

		schema = configurator.getSchema();
	}

	@Override
	public Schema schema() {
		return schema;
	}
}
