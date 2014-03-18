package uk.co.strangeskies.modabi.xml.test;

import uk.co.strangeskies.modabi.data.impl.DataTypeBuilderImpl;
import uk.co.strangeskies.modabi.impl.SchemaBuilderImpl;
import uk.co.strangeskies.modabi.model.impl.ModelBuilderImpl;
import uk.co.strangeskies.modabi.processing.SchemaBinder;
import uk.co.strangeskies.modabi.processing.impl.SchemaBinderImpl;
import uk.co.strangeskies.modabi.xml.impl.XMLOutput;

public class SchemaTest {
	private void run() {
		SchemaBinder schemaBinder = new SchemaBinderImpl(new SchemaBuilderImpl(),
				new ModelBuilderImpl(), new DataTypeBuilderImpl());
		schemaBinder.processOutput(schemaBinder.getMetaSchema().getSchemaModel(),
				new XMLOutput(), schemaBinder.getMetaSchema());
	}

	public static void main(String... args) {
		new SchemaTest().run();
	}
}
