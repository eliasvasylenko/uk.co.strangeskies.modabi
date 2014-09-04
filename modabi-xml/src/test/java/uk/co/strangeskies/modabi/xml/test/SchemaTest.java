package uk.co.strangeskies.modabi.xml.test;

import uk.co.strangeskies.modabi.data.impl.DataBindingTypeBuilderImpl;
import uk.co.strangeskies.modabi.data.io.structured.BufferingStructuredDataTarget;
import uk.co.strangeskies.modabi.model.building.impl.ModelBuilderImpl;
import uk.co.strangeskies.modabi.schema.Schema;
import uk.co.strangeskies.modabi.schema.impl.SchemaBuilderImpl;
import uk.co.strangeskies.modabi.schema.processing.SchemaBinder;
import uk.co.strangeskies.modabi.schema.processing.impl.SchemaBinderImpl;
import uk.co.strangeskies.modabi.xml.impl.XMLTarget;

public class SchemaTest {
	private void run() {
		SchemaBinder schemaBinder = new SchemaBinderImpl(new SchemaBuilderImpl(),
				new ModelBuilderImpl(), new DataBindingTypeBuilderImpl());

		schemaBinder.unbind(schemaBinder.getMetaSchema().getSchemaModel(),
				new XMLTarget(System.out), schemaBinder.getBaseSchema());

		schemaBinder.unbind(schemaBinder.getMetaSchema().getSchemaModel(),
				new XMLTarget(System.out), schemaBinder.getMetaSchema());

		BufferingStructuredDataTarget out = new BufferingStructuredDataTarget();
		schemaBinder.unbind(schemaBinder.getMetaSchema().getSchemaModel(), out,
				schemaBinder.getBaseSchema());

		Schema baseSchema = schemaBinder.bind(schemaBinder.getMetaSchema()
				.getSchemaModel(), out.buffer());
	}

	public static void main(String... args) {
		new SchemaTest().run();
	}
}
