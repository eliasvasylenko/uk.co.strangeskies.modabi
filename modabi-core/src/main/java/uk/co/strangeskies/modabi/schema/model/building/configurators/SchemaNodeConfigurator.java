package uk.co.strangeskies.modabi.schema.model.building.configurators;

import uk.co.strangeskies.modabi.namespace.Namespace;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.model.nodes.SchemaNode;
import uk.co.strangeskies.utilities.factory.Factory;

public interface SchemaNodeConfigurator<S extends SchemaNodeConfigurator<S, N>, N extends SchemaNode<?, ?>>
		extends Factory<N> {
	public S name(QualifiedName name);

	public default S name(String name, Namespace namespace) {
		return name(new QualifiedName(name, namespace));
	}

	S isAbstract(boolean isAbstract);
}
