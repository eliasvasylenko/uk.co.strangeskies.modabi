package uk.co.strangeskies.modabi.data.io;

public interface TerminatingDataSource extends DataSource {
	public BufferedDataSource buffer();

	int size();

	default <T extends DataTarget> T pipe(T target) {
		return pipe(target, size());
	}
}
