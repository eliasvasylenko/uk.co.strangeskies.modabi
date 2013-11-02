package uk.co.strangeskies.modabi;

public interface DataNodeType<T> {
	public String getName();

	public T getData(String input);
}
