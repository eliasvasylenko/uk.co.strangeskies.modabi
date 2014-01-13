package uk.co.strangeskies.modabi;

import uk.co.strangeskies.modabi.data.DataTypeBuilder;
import uk.co.strangeskies.modabi.model.Model;
import uk.co.strangeskies.modabi.model.build.ModelBuilder;

public interface MetaSchema extends Schema {
	public void setModelBuilder(ModelBuilder nodeBuilder);

	public void setSchemaBuilder(SchemaBuilder schemaBuilder);

	public void setDataTypeBuilder(DataTypeBuilder dataType);

	public void initialise();

	public Model<Schema> getSchemaModel();
}
