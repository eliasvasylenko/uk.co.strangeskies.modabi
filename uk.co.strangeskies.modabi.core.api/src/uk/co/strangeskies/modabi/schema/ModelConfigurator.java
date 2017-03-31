package uk.co.strangeskies.modabi.schema;

import uk.co.strangeskies.reflection.token.TypeToken;

public interface ModelConfigurator extends BindingPointConfigurator<ModelConfigurator> {
	@Override
	default <V> SchemaNodeConfigurator<V, ModelFactory<V>> withNodeOfType(Class<V> dataType) {
		return withNodeOfType(TypeToken.forClass(dataType));
	}

	@Override
	<V> SchemaNodeConfigurator<V, ModelFactory<V>> withNodeOfType(TypeToken<V> dataType);

	@Override
	default <V> ModelFactory<V> withoutNode(Class<V> dataType) {
		return withoutNode(TypeToken.forClass(dataType));
	}

	@Override
	<V> ModelFactory<V> withoutNode(TypeToken<V> dataType);

	@Override
	default <V> SchemaNodeConfigurator<V, ModelFactory<V>> withNodeOfModel(Model<V> baseModel) {
		return withNodeOfType(baseModel.dataType()).baseModel(baseModel);
	}
}
