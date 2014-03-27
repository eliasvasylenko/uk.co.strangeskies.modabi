package uk.co.strangeskies.modabi.model.building;

import uk.co.strangeskies.gears.mathematics.Range;
import uk.co.strangeskies.modabi.model.Model;
import uk.co.strangeskies.modabi.model.nodes.ElementNode;

public interface ElementNodeConfigurator<T> extends
		AbstractModelConfigurator<ElementNodeConfigurator<T>, ElementNode<T>, T>,
		BindingChildNodeConfigurator<ElementNodeConfigurator<T>, ElementNode<T>, T> {
	@Override
	public <V extends T> ElementNodeConfigurator<V> baseModel(
			@SuppressWarnings("unchecked") Model<? super V>... baseModel);

	@Override
	public <V extends T> ElementNodeConfigurator<V> dataClass(Class<V> dataClass);

	@Override
	public ElementNodeConfigurator<T> occurances(Range<Integer> occuranceRange);
}
