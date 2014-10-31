package uk.co.strangeskies.modabi.example.person;

import uk.co.strangeskies.modabi.schema.processing.SchemaManager;
import uk.co.strangeskies.modabi.schema.processing.impl.SchemaManagerImpl;
import uk.co.strangeskies.modabi.xml.impl.XMLSource;

public class PersonTest {
	public static void main(String... args) {
		SchemaManager manager = new SchemaManagerImpl();

		manager
				.registerSchemaBinding(new XMLSource(
						PersonTest.class
								.getResourceAsStream("/uk/co/strangeskies/modabi/example/person/BenchmarkSchema.xml")));
	}
}
