package uk.co.strangeskies.modabi.schema.model.building.configurators;

import java.util.function.Function;

import org.apache.commons.collections4.Factory;

import uk.co.strangeskies.modabi.namespace.Namespace;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.model.building.ChildBuilder;
import uk.co.strangeskies.modabi.schema.model.nodes.BindingChildNode;
import uk.co.strangeskies.modabi.schema.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.schema.model.nodes.SchemaNode;

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
