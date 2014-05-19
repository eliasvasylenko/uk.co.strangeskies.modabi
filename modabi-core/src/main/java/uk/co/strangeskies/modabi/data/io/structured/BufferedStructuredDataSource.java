package uk.co.strangeskies.modabi.data.io.structured;

import uk.co.strangeskies.gears.utilities.Copyable;

public interface BufferedStructuredDataSource extends StructuredDataSource,
		Copyable<BufferedStructuredDataSource> {
	void reset();
}
