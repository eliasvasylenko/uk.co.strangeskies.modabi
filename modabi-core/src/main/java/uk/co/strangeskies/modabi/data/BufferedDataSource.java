package uk.co.strangeskies.modabi.data;

public interface BufferedDataSource extends DataSource {
	BufferedDataSource reset();

	int size();

	default BufferedDataSource dump(DataTarget target) {
		return dump(target, size());
	}

	BufferedDataSource dump(DataTarget target, int items);
}
