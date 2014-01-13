package uk.co.strangeskies.modabi;

import uk.co.strangeskies.modabi.data.DataTypes;
import uk.co.strangeskies.modabi.model.Models;
import uk.co.strangeskies.modabi.namespace.QualifiedName;

public interface Schema {
	public QualifiedName getQualifiedName();

	public Schemata getDependencies();

	public DataTypes getDataTypes();

	public Models getModels();
}
