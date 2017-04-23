package uk.co.strangeskies.modabi.schema;

import java.util.Optional;

import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.reflection.token.TypeToken;

public interface BindingPointConfigurator<S extends BindingPointConfigurator<S>> {
	S name(QualifiedName name);

	default S name(String name, Namespace namespace) {
		return name(new QualifiedName(name, namespace));
	}

	Optional<QualifiedName> getName();

	SchemaNodeConfigurator<?, ?> withNode();

	<V> SchemaNodeConfigurator<V, ?> withNode(TypeToken<V> dataType);

	<V> SchemaNodeConfigurator<V, ?> withNode(Class<V> dataType);

	<V> SchemaNodeConfigurator<V, ?> withNode(Model<V> baseModel);

	<V> Object withoutNode(TypeToken<V> dataType);

	<V> Object withoutNode(Class<V> dataType);

	Optional<QualifiedName> getNode();
}
