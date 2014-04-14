package uk.co.strangeskies.modabi.data;

public interface BufferingDataTarget extends DataTarget {
	public abstract BufferedDataSource buffer();
}