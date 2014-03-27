package uk.co.strangeskies.modabi.model.nodes;

import java.lang.reflect.Method;

import uk.co.strangeskies.gears.mathematics.Range;

public interface BindingChildNode<T> extends BindingNode<T>, InputNode {
	Method getOutMethod();

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
