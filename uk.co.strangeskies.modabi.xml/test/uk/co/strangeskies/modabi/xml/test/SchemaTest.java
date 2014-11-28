package uk.co.strangeskies.modabi.xml.test;

import uk.co.strangeskies.modabi.io.structured.BufferedStructuredDataSource;
import uk.co.strangeskies.modabi.io.structured.BufferingStructuredDataTarget;
import uk.co.strangeskies.modabi.schema.Schema;
import uk.co.strangeskies.modabi.schema.management.SchemaManager;
import uk.co.strangeskies.modabi.schema.management.impl.SchemaManagerImpl;
import uk.co.strangeskies.modabi.xml.impl.XMLTarget;

public class SchemaTest {
	public static void main(String... args) {
		new SchemaTest().run(new SchemaManagerImpl());
	}

	public void run(SchemaManager schemaManager) {
		System.out.println("Creating SchemaManager...");

		System.out.println("Unbinding MetaSchema...");
		BufferingStructuredDataTarget out = new BufferingStructuredDataTarget();
		schemaManager.unbind(schemaManager.getMetaSchema().getSchemaModel(), out,
				schemaManager.getMetaSchema());
		BufferedStructuredDataSource buffered = out.buffer();

		buffered.pipeNextChild(new XMLTarget(System.out));
		buffered.reset();

		System.out.println("Re-binding MetaSchema...");
		Schema metaSchema = schemaManager.bind(schemaManager.getMetaSchema()
				.getSchemaModel(), buffered);

		System.out.println("Success: "
				+ metaSchema.equals(schemaManager.getMetaSchema()));

		System.out.print("Profiling Preparation");
		for (int i = 1; i <= 80; i++) {
			if (i % 50 == 0)
				System.out.println();
			System.out.print(".");

			schemaManager.unbind(schemaManager.getMetaSchema().getSchemaModel(),
					new BufferingStructuredDataTarget(), schemaManager.getMetaSchema());

			buffered.reset();
			schemaManager.bind(schemaManager.getMetaSchema().getSchemaModel(),
					buffered);
		}
		System.out.println();

		int profileRounds = 40;

		System.out.print("Unbinding Profiling");
		long startTime = System.currentTimeMillis();
		for (int i = 1; i <= profileRounds; i++) {
			if (i % 50 == 0)
				System.out.println();
			System.out.print(".");

			schemaManager.unbind(schemaManager.getMetaSchema().getSchemaModel(),
					new BufferingStructuredDataTarget(), schemaManager.getMetaSchema());
		}
		long totalTimeUnbinding = System.currentTimeMillis() - startTime;
		System.out.println();

		System.out.print("Binding Profiling");
		startTime = System.currentTimeMillis();
		for (int i = 1; i <= profileRounds; i++) {
			if (i % 50 == 0)
				System.out.println();
			System.out.print(".");

			buffered.reset();
			schemaManager.bind(schemaManager.getMetaSchema().getSchemaModel(),
					buffered);
		}
		long totalTimeBinding = System.currentTimeMillis() - startTime;
		System.out.println();

		System.out.println("Time per unbind: " + (double) totalTimeUnbinding
				/ (profileRounds * 1000) + " seconds");
		System.out.println("Time per bind: " + (double) totalTimeBinding
				/ (profileRounds * 1000) + " seconds");
	}
}
