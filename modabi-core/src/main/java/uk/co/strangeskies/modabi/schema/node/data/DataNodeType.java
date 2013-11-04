package uk.co.strangeskies.modabi.schema.node.data;

public interface DataNodeType<T> {
	public String getName();

	public T getData(String input);
}
