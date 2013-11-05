package uk.co.strangeskies.modabi.schema;

import java.util.Collection;

import uk.co.strangeskies.gears.utilities.Factory;
import uk.co.strangeskies.modabi.schema.node.ElementSchemaNode;
import uk.co.strangeskies.modabi.schema.node.data.DataNodeType;

public interface BindingSchemaBuilder<T> extends Factory<BindingSchema<T>> {
	public BindingSchemaBuilder<T> includes(
			Collection<? extends BindingSchema<?>> includes);

	public BindingSchemaBuilder<T> types(
			Collection<? extends DataNodeType<?>> types);

	public BindingSchemaBuilder<T> models(
			Collection<? extends ElementSchemaNode<?>> models);

	public <U extends T> BindingSchemaBuilder<U> root(
			ElementSchemaNode<U> rootNode);
}
