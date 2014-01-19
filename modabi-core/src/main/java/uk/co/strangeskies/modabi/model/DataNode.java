package uk.co.strangeskies.modabi.model;

public interface DataNode<T> extends InputNode {
	public String getOutMethod();

	/**
	 * If this method returns true, the return value of any invocation of the
	 * inMethod will replace the build class of any
	 *
	 * @return
	 */
	public Boolean isOutMethodIterable();

	public Class<T> getDataClass();
}
