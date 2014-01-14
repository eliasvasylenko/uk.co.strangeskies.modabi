package uk.co.strangeskies.modabi.model.building;

import uk.co.strangeskies.gears.mathematics.Range;
import uk.co.strangeskies.modabi.model.RepeatableNode;

public interface RepeatableNodeConfigurator<S extends RepeatableNodeConfigurator<S, N>, N extends RepeatableNode>
		extends SchemaNodeConfigurator<S, N> {
	public S occurances(Range<Integer> occuranceRange);
}
