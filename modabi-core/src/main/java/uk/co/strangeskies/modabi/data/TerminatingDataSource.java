package uk.co.strangeskies.modabi.data;

public interface TerminatingDataSource extends DataSource {
	public BufferedDataSource buffer();
}
