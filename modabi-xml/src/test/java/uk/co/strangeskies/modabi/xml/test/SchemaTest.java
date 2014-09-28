package uk.co.strangeskies.modabi.xml.test;

import uk.co.strangeskies.modabi.data.impl.DataBindingTypeBuilderImpl;
import uk.co.strangeskies.modabi.data.io.structured.BufferedStructuredDataSource;
import uk.co.strangeskies.modabi.data.io.structured.BufferingStructuredDataTarget;
import uk.co.strangeskies.modabi.schema.Schema;
import uk.co.strangeskies.modabi.schema.impl.SchemaBuilderImpl;
import uk.co.strangeskies.modabi.schema.model.building.impl.ModelBuilderImpl;
import uk.co.strangeskies.modabi.schema.processing.SchemaManager;
import uk.co.strangeskies.modabi.schema.processing.impl.SchemaManagerImpl;

public class SchemaTest {
	private void run() {
		System.out.println("Creating SchemaManager...");
		SchemaManager schemaBinder = new SchemaManagerImpl(new SchemaBuilderImpl(),
				new ModelBuilderImpl(), new DataBindingTypeBuilderImpl());

		System.out.println("Unbinding MetaSchema...");
		BufferingStructuredDataTarget out = new BufferingStructuredDataTarget();
		schemaBinder.unbind(schemaBinder.getMetaSchema().getSchemaModel(), out,
				schemaBinder.getMetaSchema());
		BufferedStructuredDataSource buffered = out.buffer();

		System.out.println("Re-binding MetaSchema...");
		Schema metaSchema = schemaBinder.bind(schemaBinder.getMetaSchema()
				.getSchemaModel(), buffered);

		System.out.println("Success: "
				+ metaSchema.equals(schemaBinder.getMetaSchema()));
	}

	public static void main(String... args) {
		new SchemaTest().run();
	}
}
