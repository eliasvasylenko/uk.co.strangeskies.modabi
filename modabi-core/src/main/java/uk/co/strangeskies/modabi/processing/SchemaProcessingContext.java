package uk.co.strangeskies.modabi.processing;

import uk.co.strangeskies.modabi.model.nodes.ChoiceNode;
import uk.co.strangeskies.modabi.model.nodes.DataNode;
import uk.co.strangeskies.modabi.model.nodes.ElementNode;
import uk.co.strangeskies.modabi.model.nodes.SequenceNode;

public interface SchemaProcessingContext {
	public <U> void accept(ElementNode<U> node);

	public <U> void accept(DataNode<U> node);

	public void accept(SequenceNode node);

	public void accept(ChoiceNode node);
}
