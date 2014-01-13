package uk.co.strangeskies.modabi.model;

import uk.co.strangeskies.gears.mathematics.Range;

public interface RepeatableNode extends SchemaNode {
	public Range<Integer> getOccurances();
}
