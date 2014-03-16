package uk.co.strangeskies.modabi.model.nodes;

import java.lang.reflect.Method;

public interface DataNode<T> extends InputNode {
	public String getOutMethodName();

	public Method getOutMethod();

	/**
	 * If this method returns true, the return value of any invocation of the
	 * inMethod will replace the build class of any
	 *
	 * @return
	 */
	public Boolean isOutMethodIterable();

	public Class<T> getDataClass();
}
