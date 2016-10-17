package uk.co.strangeskies.modabi.schema;

import java.lang.reflect.AnnotatedType;
import java.util.function.Function;

import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.utilities.Factory;
import uk.co.strangeskies.utilities.Self;

public interface BindingPointConfigurator<T, S extends BindingPointConfigurator<T, S>>
		extends Self<S>, Factory<BindingPoint<T>> {
	S name(QualifiedName name);

	S name(String name);

	default S name(String name, Namespace namespace) {
		return name(new QualifiedName(name, namespace));
	}

	QualifiedName getName();

	S concrete(boolean concrete);

	Boolean getConcrete();

	S export(boolean export);

	Boolean getExport();

	default <V> BindingPointConfigurator<V, ?> dataType(Class<V> dataType) {
		return dataType(TypeToken.overType(dataType));
	}

	<V> BindingPointConfigurator<V, ?> dataType(TypeToken<? extends V> dataType);

	default BindingPointConfigurator<?, ?> dataType(AnnotatedType dataType) {
		return dataType(TypeToken.overAnnotatedType(dataType));
	}

	<V> BindingPointConfigurator<V, ?> baseModel(Model<? extends V> dataType);

	SchemaNodeConfigurator node();

	default S node(Function<SchemaNodeConfigurator, SchemaNodeConfigurator> configuration) {
		configuration.apply(node()).create();

		return getThis();
	}

	TypeToken<T> getDataType();

	SchemaNode getNode();
}
