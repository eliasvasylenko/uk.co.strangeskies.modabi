package uk.co.strangeskies.modabi.schema;

import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.node.model.Models;
import uk.co.strangeskies.modabi.schema.node.type.DataBindingTypes;

public interface Schema {
	public QualifiedName getQualifiedName();

	public Schemata getDependencies();

	public DataBindingTypes getDataTypes();

	public Models getModels();
}
