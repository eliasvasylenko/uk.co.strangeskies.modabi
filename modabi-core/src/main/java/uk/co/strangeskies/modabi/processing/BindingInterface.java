package uk.co.strangeskies.modabi.processing;

import java.lang.reflect.Method;
import java.util.List;

import uk.co.strangeskies.modabi.model.nodes.SchemaNode;

public interface BindingInterface<T> {
	String getName();

	Class<T> getDataClass();

	Class<?> getBindingClass();

	BindingStrategy getBindingStrategy();

	Class<?> getUnbindingClass();

	Method getUnbindingMethod();

	UnbindingStrategy getUnbindingStrategy();

	List<? extends SchemaNode> getChildren();
}
