package uk.co.strangeskies.modabi;

import java.util.function.Function;

import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.namespace.QualifiedNamedSet;

public class Schemata extends QualifiedNamedSet<Schema> {
	public Schemata() {
		super(new Function<Schema, QualifiedName>() {
			@Override
			public QualifiedName apply(Schema t) {
				return t.getQualifiedName();
			}
		});
	}
}
