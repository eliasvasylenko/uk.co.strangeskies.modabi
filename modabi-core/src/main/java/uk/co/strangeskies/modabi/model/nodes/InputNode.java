package uk.co.strangeskies.modabi.model.nodes;

import java.lang.reflect.Method;

public interface InputNode<E extends InputNode.Effective<E>> extends
		ChildNode<E> {
	interface Effective<E extends Effective<E>> extends InputNode<E>,
			ChildNode.Effective<E> {
		@Override
		default Class<?> getPreInputClass() {
			return getInMethod() == null ? null : getInMethod().getDeclaringClass();
		}

		@Override
		default Class<?> getPostInputClass() {
			return (isInMethodChained() == null) ? null
					: (!isInMethodChained() ? getPreInputClass()
							: (getInMethod() == null ? null : getInMethod().getReturnType()));
		}

		Method getInMethod();
	}

	String getInMethodName();

	/**
	 * If this method returns true, the return value of any invocation of the
	 * inMethod will replace the build class of any
	 *
	 * @return
	 */
	Boolean isInMethodChained();
}
