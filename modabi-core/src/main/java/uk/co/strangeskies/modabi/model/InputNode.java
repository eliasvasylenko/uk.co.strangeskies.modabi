package uk.co.strangeskies.modabi.model;

import java.lang.reflect.Method;

public interface InputNode extends SchemaNode {
	public String getInMethodName();

	public Method getInMethod();

	/**
	 * If this method returns true, the return value of any invocation of the
	 * inMethod will replace the build class of any
	 *
	 * @return
	 */
	public Boolean isInMethodChained();
}
