package uk.co.strangeskies.modabi.model.nodes;

import uk.co.strangeskies.gears.mathematics.Range;
import uk.co.strangeskies.modabi.model.AbstractModel;

public interface ElementNode<T> extends AbstractModel<T>, BindingChildNode<T> {
	public Range<Integer> getOccurances();
}
