package uk.co.strangeskies.modabi.processing;

import uk.co.strangeskies.modabi.model.ChoiceNode;
import uk.co.strangeskies.modabi.model.ContentNode;
import uk.co.strangeskies.modabi.model.ElementNode;
import uk.co.strangeskies.modabi.model.PropertyNode;
import uk.co.strangeskies.modabi.model.SequenceNode;
import uk.co.strangeskies.modabi.model.SimpleElementNode;

public interface SchemaProcessingContext {
	public <T> void accept(ContentNode<T> node);

	public <T> void accept(PropertyNode<T> node);

	public <T> void accept(SimpleElementNode<T> node);

	public void accept(ChoiceNode node);

	public void accept(SequenceNode node);

	public <T> void accept(ElementNode<T> node);
}
