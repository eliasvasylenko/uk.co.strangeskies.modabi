package uk.co.strangeskies.modabi.processing;

import uk.co.strangeskies.modabi.model.ChoiceNode;
import uk.co.strangeskies.modabi.model.ContentNode;
import uk.co.strangeskies.modabi.model.ElementNode;
import uk.co.strangeskies.modabi.model.PropertyNode;
import uk.co.strangeskies.modabi.model.SequenceNode;
import uk.co.strangeskies.modabi.model.SimpleElementNode;

public interface SchemaProcessingContext<T> {
	public <U> T accept(ContentNode<U> node);

	public <U> T accept(PropertyNode<U> node);

	public <U> T accept(SimpleElementNode<U> node);

	public T accept(ChoiceNode node);

	public T accept(SequenceNode node);

	public <U> T accept(ElementNode<U> node);
}
