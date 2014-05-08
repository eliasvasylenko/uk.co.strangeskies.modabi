package uk.co.strangeskies.modabi.schema;

import java.util.Collection;

import uk.co.strangeskies.gears.utilities.factory.Factory;
import uk.co.strangeskies.modabi.data.DataType;
import uk.co.strangeskies.modabi.model.Model;
import uk.co.strangeskies.modabi.namespace.QualifiedName;

public interface SchemaConfigurator extends Factory<Schema> {
	public SchemaConfigurator qualifiedName(QualifiedName name);

	public SchemaConfigurator dependencies(
			Collection<? extends Schema> dependencies);

	public SchemaConfigurator types(Collection<? extends DataType<?>> types);

	public SchemaConfigurator models(Collection<? extends Model<?>> models);
}
