package uk.co.strangeskies.modabi.model.nodes;

import java.lang.reflect.Method;

import uk.co.strangeskies.modabi.schema.processing.BindingStrategy;
import uk.co.strangeskies.modabi.schema.processing.UnbindingStrategy;

public interface BindingNode<T> extends SchemaNode {
	Class<T> getDataClass();

	BindingStrategy getBindingStrategy();

	Class<?> getBindingClass();

	UnbindingStrategy getUnbindingStrategy();

	Class<?> getUnbindingClass();

	Method getUnbindingMethod();

	String getUnbindingMethodName();

	Class<?> getUnbindingStaticFactoryClass();
}
