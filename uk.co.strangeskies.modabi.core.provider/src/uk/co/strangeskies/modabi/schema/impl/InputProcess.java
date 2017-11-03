package uk.co.strangeskies.modabi.schema.impl;

import uk.co.strangeskies.modabi.binding.BindingContext;
import uk.co.strangeskies.modabi.schema.ChildBindingPoint;

/**
 * A class representing the process of inputting a binding result object to the
 * parent of a {@link ChildBindingPoint child binding point}.
 * 
 * @author Elias N Vasylenko
 *
 * @param <T>
 *          the type of the binding point
 */
public interface InputProcess {
	/**
	 * @param target
	 *          the parent binding object
	 * @param input
	 *          the child object result
	 * @return the new parent binding object
	 */
	Object process(BindingContext context, Object inputTarget, Object result);
}
