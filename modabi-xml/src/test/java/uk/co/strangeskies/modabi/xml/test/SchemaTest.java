package uk.co.strangeskies.modabi.xml.test;

import uk.co.strangeskies.modabi.MetaSchema;
import uk.co.strangeskies.modabi.data.impl.DataTypeBuilderImpl;
import uk.co.strangeskies.modabi.impl.MetaSchemaImpl;
import uk.co.strangeskies.modabi.impl.SchemaBuilderImpl;
import uk.co.strangeskies.modabi.model.impl.ModelBuilderImpl;
import uk.co.strangeskies.modabi.processing.SchemaBinder;
import uk.co.strangeskies.modabi.processing.impl.SchemaBinderImpl;
import uk.co.strangeskies.modabi.xml.impl.XMLOutput;

public class SchemaTest {
	private void run() {
		MetaSchema metaSchema = new MetaSchemaImpl();
		metaSchema.setDataTypeBuilder(new DataTypeBuilderImpl());
		metaSchema.setModelBuilder(new ModelBuilderImpl());
		metaSchema.setSchemaBuilder(new SchemaBuilderImpl());
		metaSchema.initialise();

		SchemaBinder schemaBinder = new SchemaBinderImpl(metaSchema);
		schemaBinder.processOutput(metaSchema.getSchemaModel(), new XMLOutput(),
				metaSchema);
	}

	public static void main(String... args) {
		new SchemaTest().run();
	}
}
