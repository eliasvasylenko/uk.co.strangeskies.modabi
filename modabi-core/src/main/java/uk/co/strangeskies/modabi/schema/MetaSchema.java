package uk.co.strangeskies.modabi.schema;

import uk.co.strangeskies.modabi.schema.model.Model;

public interface MetaSchema extends Schema {
	public Model<Schema> getSchemaModel();
}
