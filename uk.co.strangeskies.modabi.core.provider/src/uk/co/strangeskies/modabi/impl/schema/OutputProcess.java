package uk.co.strangeskies.modabi.impl.schema;

import uk.co.strangeskies.modabi.processing.ProcessingContext;
import uk.co.strangeskies.modabi.schema.ChildBindingPoint;

/**
 * A class representing the process of outputting a binding object from the
 * parent of a {@link ChildBindingPoint child binding point}.
 * 
 * @author Elias N Vasylenko
 *
 * @param <T>
 *          the type of the binding point
 */
public interface OutputProcess {
	/**
	 * @param source
	 *          the parent binding object
	 * @return the child binding object
	 */
	Object process(ProcessingContext context, Object outputSource);
}
