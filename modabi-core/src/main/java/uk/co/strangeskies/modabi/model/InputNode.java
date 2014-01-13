package uk.co.strangeskies.modabi.model;

public interface InputNode extends SchemaNode {
	public String getInMethod();

	/**
	 * If this method returns true, the return value of any invocation of the
	 * inMethod will replace the build class of any
	 * 
	 * @return
	 */
	public boolean isInMethodChained();
}
