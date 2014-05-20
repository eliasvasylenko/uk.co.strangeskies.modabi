package uk.co.strangeskies.modabi.data;

import java.lang.reflect.Method;
import java.util.List;

import uk.co.strangeskies.modabi.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.schema.processing.BindingStrategy;
import uk.co.strangeskies.modabi.schema.processing.UnbindingStrategy;

public interface DataBindingType<T> {
	String getName();

	Class<T> getDataClass();

	Class<?> getBindingClass();

	BindingStrategy getBindingStrategy();

	Class<?> getUnbindingClass();

	UnbindingStrategy getUnbindingStrategy();

	Method getUnbindingMethod();

	String getUnbindingMethodName();

	List<ChildNode> getChildren();

	List<ChildNode> getEffectiveChildren();
}
