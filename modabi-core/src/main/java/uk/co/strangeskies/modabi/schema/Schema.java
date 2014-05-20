package uk.co.strangeskies.modabi.schema;

import uk.co.strangeskies.modabi.data.DataBindingTypes;
import uk.co.strangeskies.modabi.model.Models;
import uk.co.strangeskies.modabi.namespace.QualifiedName;

public interface Schema {
	public QualifiedName getQualifiedName();

	public Schemata getDependencies();

	public DataBindingTypes getDataTypes();

	public Models getModels();
}
