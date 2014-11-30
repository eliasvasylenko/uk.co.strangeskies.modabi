package uk.co.strangeskies.modabi.schema.node.building.configuration;

import java.util.function.Function;

import uk.co.strangeskies.modabi.namespace.Namespace;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.node.SchemaNode;
import uk.co.strangeskies.modabi.schema.node.building.ChildBuilder;
import uk.co.strangeskies.utilities.factory.Factory;

public interface SchemaNodeConfigurator<S extends SchemaNodeConfigurator<S, N>, N extends SchemaNode<?, ?>>
		extends Factory<N> {
	public S name(QualifiedName name);

	public default S name(String name, Namespace namespace) {
		return name(new QualifiedName(name, namespace));
	}

	S isAbstract(boolean isAbstract);

	public ChildBuilder addChild();

	public default SchemaNodeConfigurator<?, N> addChild(
			Function<ChildBuilder, SchemaNodeConfigurator<?, ?>> builder) {
		builder.apply(addChild()).create();

		return this;
	}
}
