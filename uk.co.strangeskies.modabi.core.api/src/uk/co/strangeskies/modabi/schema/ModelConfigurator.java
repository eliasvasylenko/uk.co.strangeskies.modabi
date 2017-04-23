package uk.co.strangeskies.modabi.schema;

import java.util.Optional;

import uk.co.strangeskies.reflection.token.TypeToken;

public interface ModelConfigurator extends BindingPointConfigurator<ModelConfigurator> {
	ModelConfigurator export(boolean export);

	Optional<Boolean> getExport();

	@Override
	default SchemaNodeConfigurator<Object, ModelFactory<Object>> withNode() {
		return withNode(Object.class);
	}

	@Override
	default <V> SchemaNodeConfigurator<V, ModelFactory<V>> withNode(Class<V> dataType) {
		return withNode(TypeToken.forClass(dataType));
	}

	@Override
	<V> SchemaNodeConfigurator<V, ModelFactory<V>> withNode(TypeToken<V> dataType);

	@Override
	default <V> ModelFactory<V> withoutNode(Class<V> dataType) {
		return withoutNode(TypeToken.forClass(dataType));
	}

	@Override
	<V> ModelFactory<V> withoutNode(TypeToken<V> dataType);

	@Override
	default <V> SchemaNodeConfigurator<V, ModelFactory<V>> withNode(Model<V> baseModel) {
		return withNode(baseModel.dataType()).baseModel(baseModel);
	}
}
