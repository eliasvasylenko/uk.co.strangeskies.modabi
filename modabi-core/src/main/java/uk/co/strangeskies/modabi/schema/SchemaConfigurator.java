package uk.co.strangeskies.modabi.schema;

import java.util.Collection;
import java.util.Set;

import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.node.model.Model;
import uk.co.strangeskies.modabi.schema.node.type.DataBindingType;
import uk.co.strangeskies.utilities.factory.Factory;

public interface SchemaConfigurator extends Factory<Schema> {
	public SchemaConfigurator qualifiedName(QualifiedName name);

	public SchemaConfigurator requirements(Set<? extends Class<?>> requirements);

	public SchemaConfigurator dependencies(
			Collection<? extends Schema> dependencies);

	public SchemaConfigurator types(Collection<? extends DataBindingType<?>> types);

	public SchemaConfigurator models(Collection<? extends Model<?>> models);
}
