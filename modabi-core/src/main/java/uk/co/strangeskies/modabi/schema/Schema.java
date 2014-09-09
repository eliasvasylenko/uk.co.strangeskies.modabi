package uk.co.strangeskies.modabi.schema;

import uk.co.strangeskies.modabi.data.DataBindingTypes;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.model.Models;
import uk.co.strangeskies.modabi.schema.requirement.Requirements;

public interface Schema {
	public QualifiedName getQualifiedName();

	public Requirements getRequirements();

	public Schemata getDependencies();

	public DataBindingTypes getDataTypes();

	public Models getModels();
}
