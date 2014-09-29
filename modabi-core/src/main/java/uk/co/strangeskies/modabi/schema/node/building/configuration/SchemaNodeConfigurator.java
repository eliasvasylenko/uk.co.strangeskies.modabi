package uk.co.strangeskies.modabi.schema.node.building.configuration;

import java.util.function.Function;

import org.apache.commons.collections4.Factory;

import uk.co.strangeskies.modabi.namespace.Namespace;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.node.BindingChildNode;
import uk.co.strangeskies.modabi.schema.node.ChildNode;
import uk.co.strangeskies.modabi.schema.node.SchemaNode;
import uk.co.strangeskies.modabi.schema.node.building.ChildBuilder;

public interface SchemaNodeConfigurator<S extends SchemaNodeConfigurator<S, N, C, B>, N extends SchemaNode<?, ?>, C extends ChildNode<?, ?>, B extends BindingChildNode<?, ?, ?>>
		extends Factory<N> {
	public S name(QualifiedName name);

	public default S name(String name, Namespace namespace) {
		return name(new QualifiedName(name, namespace));
	}

	S isAbstract(boolean isAbstract);

	public ChildBuilder<C, B> addChild();

	public default SchemaNodeConfigurator<?, N, C, B> addChild(
			Function<ChildBuilder<C, B>, SchemaNodeConfigurator<?, ? extends C, ?, ?>> builder) {
		builder.apply(addChild()).create();

		return this;
	}
}
