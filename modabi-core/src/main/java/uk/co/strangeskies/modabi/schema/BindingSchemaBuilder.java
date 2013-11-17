package uk.co.strangeskies.modabi.schema;

import java.util.Collection;

import uk.co.strangeskies.gears.utilities.Factory;
import uk.co.strangeskies.modabi.schema.node.ElementSchemaNode;
import uk.co.strangeskies.modabi.schema.node.data.SchemaDataType;
import uk.co.strangeskies.modabi.schema.processing.SchemaProcessingContext;

public interface BindingSchemaBuilder<T, U extends SchemaProcessingContext<U>>
		extends Factory<BindingSchema<T, U>> {
	public BindingSchemaBuilder<T, U> includes(
			Collection<? extends BindingSchema<?, ? super U>> includes);

	public BindingSchemaBuilder<T, U> types(
			Collection<? extends SchemaDataType<?>> types);

	public BindingSchemaBuilder<T, U> models(
			Collection<? extends ElementSchemaNode<?, ? super U>> models);

	public <V extends T> BindingSchemaBuilder<V, U> root(
			ElementSchemaNode<V, ? super U> rootNode);
}
