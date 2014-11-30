package uk.co.strangeskies.modabi.io.structured;

import uk.co.strangeskies.utilities.Copyable;

public interface BufferedStructuredDataSource extends StructuredDataSource,
		Copyable<BufferedStructuredDataSource> {
	void reset();
}
