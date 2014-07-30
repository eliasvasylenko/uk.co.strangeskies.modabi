package uk.co.strangeskies.modabi.model.nodes;

import java.lang.reflect.Method;

import uk.co.strangeskies.gears.mathematics.Range;

public interface BindingChildNode<T, E extends BindingChildNode.Effective<T, E>>
		extends BindingNode<T, E>, InputNode<E> {
	interface Effective<T, E extends Effective<T, E>> extends
			BindingChildNode<T, E>, BindingNode.Effective<T, E>,
			InputNode.Effective<E> {
		Method getOutMethod();
	}

	String getOutMethodName();

	/**
	 * If this method returns true, the return value of any invocation of the
	 * inMethod will replace the build class of any
	 *
	 * @return
	 */
	Boolean isOutMethodIterable();

	Range<Integer> occurances();
}
