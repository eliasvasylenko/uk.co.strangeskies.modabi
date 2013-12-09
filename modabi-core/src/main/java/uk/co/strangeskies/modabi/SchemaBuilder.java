package uk.co.strangeskies.modabi;

import java.util.Collection;

import uk.co.strangeskies.gears.utilities.Factory;
import uk.co.strangeskies.modabi.data.DataType;
import uk.co.strangeskies.modabi.node.BindingNode;

public interface SchemaBuilder<T> extends Factory<Schema<T>> {
	public SchemaBuilder<T> includes(Collection<? extends Schema<?>> includes);

	public SchemaBuilder<T> types(Collection<? extends DataType<?>> types);

	public SchemaBuilder<T> models(Collection<? extends BindingNode<?>> models);

	public <V extends T> SchemaBuilder<V> root(BindingNode<V> rootNode);
}
