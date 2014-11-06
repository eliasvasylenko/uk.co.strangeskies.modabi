package uk.co.strangeskies.modabi.example.person;

import uk.co.strangeskies.modabi.schema.processing.SchemaManager;
import uk.co.strangeskies.modabi.schema.processing.impl.SchemaManagerImpl;
import uk.co.strangeskies.modabi.xml.impl.XMLSource;

public class PersonTest {
	public static void main(String... args) {
		new PersonTest().run();
	}

	private void run() {
		SchemaManager manager = new SchemaManagerImpl();

		manager.registerSchemaBinding(new XMLSource(getClass().getResourceAsStream(
				"/uk/co/strangeskies/modabi/example/person/PersonSchema.xml")));

		Person person = manager.bind(
				Person.class,
				new XMLSource(getClass().getResourceAsStream(
						"/uk/co/strangeskies/modabi/example/person/PersonData.xml")));

		System.out.println(person.getFirstName() + ", " + person.getSecondName());
	}
}
