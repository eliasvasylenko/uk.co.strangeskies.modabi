package uk.co.strangeskies.modabi.processing;

import uk.co.strangeskies.modabi.node.BindingNode;
import uk.co.strangeskies.modabi.node.ChoiceNode;
import uk.co.strangeskies.modabi.node.DataNode;
import uk.co.strangeskies.modabi.node.PropertyNode;
import uk.co.strangeskies.modabi.node.SequenceNode;

public interface SchemaProcessingContext<T extends SchemaProcessingContext<T>> {
	public void accept(DataNode<?> node);

	public void accept(PropertyNode<?> node);

	public void accept(ChoiceNode<? super T> node);

	public void accept(SequenceNode<? super T> node);

	public void accept(BindingNode<?, ? super T> node);
}
