package uk.co.strangeskies.modabi.schema;

import java.util.Set;

import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.node.model.Models;
import uk.co.strangeskies.modabi.schema.node.type.DataBindingTypes;

public interface Schema {
	public QualifiedName getQualifiedName();

	public Set<Class<?>> getRequirements();

	public Schemata getDependencies();

	public DataBindingTypes getDataTypes();

	public Models getModels();
}
