package uk.co.strangeskies.modabi.processing;

import uk.co.strangeskies.modabi.node.BindingNode;
import uk.co.strangeskies.modabi.node.ChoiceNode;
import uk.co.strangeskies.modabi.node.DataNode;
import uk.co.strangeskies.modabi.node.PropertyNode;
import uk.co.strangeskies.modabi.node.SequenceNode;

public interface SchemaProcessingContext {
	public <T> void accept(DataNode<T> node);

	public <T> void accept(PropertyNode<T> node);

	public void accept(ChoiceNode node);

	public void accept(SequenceNode node);

	public <T> void accept(BindingNode<T> node);
}
