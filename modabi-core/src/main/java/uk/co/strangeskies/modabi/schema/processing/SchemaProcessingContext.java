package uk.co.strangeskies.modabi.schema.processing;

import uk.co.strangeskies.modabi.schema.node.SequenceNode;
import uk.co.strangeskies.modabi.schema.node.DataNode;
import uk.co.strangeskies.modabi.schema.node.BindingNode;
import uk.co.strangeskies.modabi.schema.node.PropertyNode;

public interface SchemaProcessingContext<T extends SchemaProcessingContext<T>> {
	public void data(DataNode<?> node);

	public void property(PropertyNode<?> node);

	public void branch(SequenceNode<? super T> node);

	public void element(BindingNode<?, ? super T> node);
}
