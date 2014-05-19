package uk.co.strangeskies.modabi.data.io;

import uk.co.strangeskies.gears.utilities.Copyable;

public interface BufferedDataSource extends TerminatingDataSource,
		Copyable<BufferedDataSource> {
	void reset();
}
