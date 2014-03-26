package uk.co.strangeskies.modabi.processing;

import uk.co.strangeskies.modabi.model.Model;
import uk.co.strangeskies.modabi.model.nodes.ChoiceNode;
import uk.co.strangeskies.modabi.model.nodes.DataNode;
import uk.co.strangeskies.modabi.model.nodes.ElementNode;
import uk.co.strangeskies.modabi.model.nodes.SequenceNode;

public interface SchemaResultProcessingContext<T> {
	public <U> T accept(Model<U> node);

	public <U> T accept(DataNode<U> node);

	public T accept(ChoiceNode node);

	public T accept(SequenceNode node);

	public <U> T accept(ElementNode<U> node);
}
