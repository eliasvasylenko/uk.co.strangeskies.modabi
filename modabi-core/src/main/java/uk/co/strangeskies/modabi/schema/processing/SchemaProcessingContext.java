package uk.co.strangeskies.modabi.schema.processing;

import uk.co.strangeskies.modabi.schema.model.nodes.ChoiceNode;
import uk.co.strangeskies.modabi.schema.model.nodes.DataNode;
import uk.co.strangeskies.modabi.schema.model.nodes.ElementNode;
import uk.co.strangeskies.modabi.schema.model.nodes.InputSequenceNode;
import uk.co.strangeskies.modabi.schema.model.nodes.SequenceNode;

public interface SchemaProcessingContext {
	public <U> void accept(ElementNode.Effective<U> node);

	public <U> void accept(DataNode.Effective<U> node);

	public void accept(InputSequenceNode.Effective node);

	public void accept(SequenceNode.Effective node);

	public void accept(ChoiceNode.Effective node);
}
