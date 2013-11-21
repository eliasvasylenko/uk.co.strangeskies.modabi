package uk.co.strangeskies.modabi.schema;

import java.util.Collection;

import uk.co.strangeskies.gears.utilities.Factory;
import uk.co.strangeskies.modabi.schema.node.BindingNode;
import uk.co.strangeskies.modabi.schema.node.data.DataType;
import uk.co.strangeskies.modabi.schema.processing.SchemaProcessingContext;

public interface SchemaBuilder<T, U extends SchemaProcessingContext<U>>
		extends Factory<Schema<T, U>> {
	public SchemaBuilder<T, U> includes(
			Collection<? extends Schema<?, ? super U>> includes);

	public SchemaBuilder<T, U> types(
			Collection<? extends DataType<?>> types);

	public SchemaBuilder<T, U> models(
			Collection<? extends BindingNode<?, ? super U>> models);

	public <V extends T> SchemaBuilder<V, U> root(
			BindingNode<V, ? super U> rootNode);
}
