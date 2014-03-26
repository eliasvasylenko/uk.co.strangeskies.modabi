package uk.co.strangeskies.modabi.model.nodes;

import java.lang.reflect.Method;

public interface InputNode extends ChildNode {
	public String getInMethodName();

	public Method getInMethod();

	/**
	 * If this method returns true, the return value of any invocation of the
	 * inMethod will replace the build class of any
	 *
	 * @return
	 */
	public Boolean isInMethodChained();

	@Override
	public default Class<?> getPreInputClass() {
		return getInMethod() == null ? null : getInMethod().getDeclaringClass();
	}

	@Override
	public default Class<?> getPostInputClass() {
		return (isInMethodChained() == null) ? null
				: (!isInMethodChained() ? getPreInputClass()
						: (getInMethod() == null ? null : getInMethod().getReturnType()));
	}
}
