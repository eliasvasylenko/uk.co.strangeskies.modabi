package uk.co.strangeskies.modabi.data;

import java.util.List;

import uk.co.strangeskies.modabi.model.nodes.PropertyNode;
import uk.co.strangeskies.modabi.processing.BindingStrategy;
import uk.co.strangeskies.modabi.processing.UnbindingStrategy;

public interface DataType<T> {
	String getName();

	Class<T> getDataClass();

	Class<?> getBindingClass();

	BindingStrategy getBindingStrategy();

	Class<?> getUnbindingClass();

	UnbindingStrategy getUnbindingStrategy();

	List<PropertyNode<?>> getProperties();

	boolean isPrimitive();
}
