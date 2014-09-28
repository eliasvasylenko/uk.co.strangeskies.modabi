package uk.co.strangeskies.modabi.schema;

import java.util.Set;

import uk.co.strangeskies.modabi.data.DataBindingTypes;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.model.Models;

public interface Schema {
	public QualifiedName getQualifiedName();

	public Set<Class<?>> getRequirements();

	public Schemata getDependencies();

	public DataBindingTypes getDataTypes();

	public Models getModels();
}
