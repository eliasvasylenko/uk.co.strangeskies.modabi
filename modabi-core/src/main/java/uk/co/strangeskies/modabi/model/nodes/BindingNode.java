package uk.co.strangeskies.modabi.model.nodes;

import java.lang.reflect.Method;

import uk.co.strangeskies.modabi.processing.BindingStrategy;
import uk.co.strangeskies.modabi.processing.UnbindingStrategy;

public interface BindingNode<T> extends SchemaNode {
	Class<T> getDataClass();

	BindingStrategy getBindingStrategy();

	Class<?> getBindingClass();

	UnbindingStrategy getUnbindingStrategy();

	Class<?> getUnbindingClass();

	Method getUnbindingMethod();
}
