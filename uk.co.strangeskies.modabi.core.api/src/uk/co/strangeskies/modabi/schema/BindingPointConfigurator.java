package uk.co.strangeskies.modabi.schema;

import static java.util.Arrays.asList;

import java.lang.reflect.AnnotatedType;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.reflection.token.TypeToken;
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

	<V> BindingPointConfigurator<V, ?> dataType(TypeToken<? super V> dataType);

	default BindingPointConfigurator<?, ?> dataType(AnnotatedType dataType) {
		return dataType(TypeToken.overAnnotatedType(dataType));
	}

	TypeToken<T> getDataType();

	@SuppressWarnings("unchecked")
	default <V> BindingPointConfigurator<V, ?> baseModel(Model<? super V> baseModel) {
		return (BindingPointConfigurator<V, ?>) baseModel(asList(baseModel));
	}

	BindingPointConfigurator<?, ?> baseModel(Collection<? extends Model<?>> baseModel);

	List<Model<?>> getBaseModel();

	SchemaNodeConfigurator node();

	default S node(Function<SchemaNodeConfigurator, SchemaNodeConfigurator> configuration) {
		configuration.apply(node()).create();

		return getThis();
	}

	SchemaNode getNode();
}
