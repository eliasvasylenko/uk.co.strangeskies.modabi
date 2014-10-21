package uk.co.strangeskies.modabi.xml.test;

import uk.co.strangeskies.modabi.io.structured.BufferedStructuredDataSource;
import uk.co.strangeskies.modabi.io.structured.BufferingStructuredDataTarget;
import uk.co.strangeskies.modabi.schema.Schema;
import uk.co.strangeskies.modabi.schema.impl.SchemaBuilderImpl;
import uk.co.strangeskies.modabi.schema.node.model.impl.ModelBuilderImpl;
import uk.co.strangeskies.modabi.schema.node.type.impl.DataBindingTypeBuilderImpl;
import uk.co.strangeskies.modabi.schema.processing.SchemaManager;
import uk.co.strangeskies.modabi.schema.processing.impl.SchemaManagerImpl;

public class SchemaTest {
	private void run() {
		System.out.println("Creating SchemaManager...");
		SchemaManager schemaBinder = new SchemaManagerImpl(new SchemaBuilderImpl(),
				new ModelBuilderImpl(), new DataBindingTypeBuilderImpl());

		for (int i = 0; i < 20; i++) {
			System.out.println("Unbinding MetaSchema...");
			BufferingStructuredDataTarget out = new BufferingStructuredDataTarget();
			schemaBinder.unbind(schemaBinder.getMetaSchema().getSchemaModel(), out,
					schemaBinder.getMetaSchema());
			BufferedStructuredDataSource buffered = out.buffer();

			// buffered.pipeNextChild(new XMLTarget(System.out)); buffered.reset();

			System.out.println("Re-binding MetaSchema...");
			Schema metaSchema = schemaBinder.bind(schemaBinder.getMetaSchema()
					.getSchemaModel(), buffered);

			System.out.println("Success: "
					+ metaSchema.equals(schemaBinder.getMetaSchema()));
		}

		long totalTimeBinding = 0;
		long totalTimeUnbinding = 0;

		for (int i = 0; i < 20; i++) {
			long startTime = System.currentTimeMillis();

			System.out.println("Unbinding MetaSchema...");
			BufferingStructuredDataTarget out = new BufferingStructuredDataTarget();
			schemaBinder.unbind(schemaBinder.getMetaSchema().getSchemaModel(), out,
					schemaBinder.getMetaSchema());
			BufferedStructuredDataSource buffered = out.buffer();

			totalTimeUnbinding += System.currentTimeMillis() - startTime;
			startTime = System.currentTimeMillis();

			System.out.println("Re-binding MetaSchema...");
			Schema metaSchema = schemaBinder.bind(schemaBinder.getMetaSchema()
					.getSchemaModel(), buffered);

			System.out.println("Success: "
					+ metaSchema.equals(schemaBinder.getMetaSchema()));

			totalTimeBinding += System.currentTimeMillis() - startTime;
		}

		System.out.println("Time per unbind: " + (double) totalTimeUnbinding
				/ 20000 + " seconds");
		System.out.println("Time per bind: " + (double) totalTimeBinding / 20000
				+ " seconds");
	}

	public static void main(String... args) {
		new SchemaTest().run();
	}
}
