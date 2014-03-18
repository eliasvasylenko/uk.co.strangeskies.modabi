package uk.co.strangeskies.modabi.processing;

import uk.co.strangeskies.modabi.model.nodes.ChoiceNode;
import uk.co.strangeskies.modabi.model.nodes.ContentNode;
import uk.co.strangeskies.modabi.model.nodes.ElementNode;
import uk.co.strangeskies.modabi.model.nodes.PropertyNode;
import uk.co.strangeskies.modabi.model.nodes.SequenceNode;
import uk.co.strangeskies.modabi.model.nodes.SimpleElementNode;

public interface SchemaProcessingContext {
	public <U> void accept(ContentNode<U> node);

	public <U> void accept(PropertyNode<U> node);

	public <U> void accept(SimpleElementNode<U> node);

	public void accept(ChoiceNode node);

	public void accept(SequenceNode node);

	public <U> void accept(ElementNode<U> node);
}
