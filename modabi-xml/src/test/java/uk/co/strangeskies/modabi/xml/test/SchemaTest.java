package uk.co.strangeskies.modabi.xml.test;

import uk.co.strangeskies.modabi.data.impl.DataBindingTypeBuilderImpl;
import uk.co.strangeskies.modabi.data.io.structured.BufferedStructuredDataSource;
import uk.co.strangeskies.modabi.data.io.structured.BufferingStructuredDataTarget;
import uk.co.strangeskies.modabi.schema.Schema;
import uk.co.strangeskies.modabi.schema.impl.SchemaBuilderImpl;
import uk.co.strangeskies.modabi.schema.model.building.impl.ModelBuilderImpl;
import uk.co.strangeskies.modabi.schema.processing.SchemaManager;
import uk.co.strangeskies.modabi.schema.processing.impl.SchemaManagerImpl;
import uk.co.strangeskies.modabi.xml.impl.XMLTarget;

public class SchemaTest {
	private void run() {
		System.out.println("Creating SchemaManager...");
		SchemaManager schemaBinder = new SchemaManagerImpl(new SchemaBuilderImpl(),
				new ModelBuilderImpl(), new DataBindingTypeBuilderImpl());

		System.out.println("Buffering MetaSchema unbinding...");
		BufferingStructuredDataTarget out = new BufferingStructuredDataTarget();
		schemaBinder.unbind(schemaBinder.getMetaSchema().getSchemaModel(), out,
				schemaBinder.getMetaSchema());

		System.out.println("Outputting buffered MetaSchema unbinding...");
		BufferedStructuredDataSource buffered = out.buffer();
		buffered.pipeNextChild(new XMLTarget(System.out));
		buffered.reset();

		System.out.println("Re-binding buffered MetaSchema unbinding...");
		Schema baseSchema = schemaBinder
				.bindFuture(schemaBinder.getMetaSchema().getSchemaModel(), buffered)
				.resolve().getData();

		System.out.println("Re-unbinding re-bound MetaSchema...");
		schemaBinder.unbind(schemaBinder.getMetaSchema().getSchemaModel(),
				new XMLTarget(System.out), baseSchema);
	}

	public static void main(String... args) {
		new SchemaTest().run();
	}
}
