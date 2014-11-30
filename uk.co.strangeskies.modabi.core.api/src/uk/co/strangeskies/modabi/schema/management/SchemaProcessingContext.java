package uk.co.strangeskies.modabi.schema.management;

import uk.co.strangeskies.modabi.schema.node.ChoiceNode;
import uk.co.strangeskies.modabi.schema.node.ComplexNode;
import uk.co.strangeskies.modabi.schema.node.DataNode;
import uk.co.strangeskies.modabi.schema.node.InputSequenceNode;
import uk.co.strangeskies.modabi.schema.node.SequenceNode;

public interface SchemaProcessingContext {
	public <U> void accept(ComplexNode.Effective<U> node);

	public <U> void accept(DataNode.Effective<U> node);

	public void accept(InputSequenceNode.Effective node);

	public void accept(SequenceNode.Effective node);

	public void accept(ChoiceNode.Effective node);
}
