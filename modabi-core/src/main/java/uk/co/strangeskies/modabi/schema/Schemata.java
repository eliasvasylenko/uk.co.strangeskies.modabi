package uk.co.strangeskies.modabi.schema;

import uk.co.strangeskies.modabi.namespace.QualifiedNamedSet;

public class Schemata extends QualifiedNamedSet<Schema> {
	public Schemata() {
		super(Schema::getQualifiedName);
	}
}
