package uk.co.strangeskies.modabi.model.nodes;

import java.lang.reflect.Method;

public interface BindingChildNode<T> extends BindingNode<T>, InputNode {
	Method getOutMethod();

	public String getOutMethodName();

	/**
	 * If this method returns true, the return value of any invocation of the
	 * inMethod will replace the build class of any
	 *
	 * @return
	 */
	public Boolean isOutMethodIterable();
}
