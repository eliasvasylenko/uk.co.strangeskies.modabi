package uk.co.strangeskies.modabi.data.io;


public interface BufferedDataSource extends TerminatingDataSource {
	void reset();

	public static BufferingDataTarget from() {
		return new BufferingDataTarget();
	}
}
