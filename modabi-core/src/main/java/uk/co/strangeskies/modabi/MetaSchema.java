package uk.co.strangeskies.modabi;

import uk.co.strangeskies.modabi.model.Model;

public interface MetaSchema extends Schema {
	public Model<Schema> getSchemaModel();
}
