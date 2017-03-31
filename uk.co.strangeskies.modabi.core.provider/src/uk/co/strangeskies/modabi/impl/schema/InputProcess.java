package uk.co.strangeskies.modabi.impl.schema;

import uk.co.strangeskies.modabi.processing.ProcessingContext;
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
	Object process(ProcessingContext context, Object inputTarget, Object result);
}
