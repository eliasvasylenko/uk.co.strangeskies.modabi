package uk.co.strangeskies.modabi.processing;

import uk.co.strangeskies.modabi.model.nodes.ChoiceNode;
import uk.co.strangeskies.modabi.model.nodes.ContentNode;
import uk.co.strangeskies.modabi.model.nodes.ElementNode;
import uk.co.strangeskies.modabi.model.nodes.PropertyNode;
import uk.co.strangeskies.modabi.model.nodes.SequenceNode;
import uk.co.strangeskies.modabi.model.nodes.SimpleElementNode;

public interface SchemaProcessingContext<T> {
	public <U> T accept(ContentNode<U> node);

	public <U> T accept(PropertyNode<U> node);

	public <U> T accept(SimpleElementNode<U> node);

	public T accept(ChoiceNode node);

	public T accept(SequenceNode node);

	public <U> T accept(ElementNode<U> node);
}
