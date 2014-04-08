package uk.co.strangeskies.modabi.data;

public interface BufferedDataSource extends DataSource {
	BufferedDataSource reset();

	@Override
	public default BufferedDataSource buffer() {
		return this;
	}
}
