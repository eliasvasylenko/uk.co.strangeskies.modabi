package uk.co.strangeskies.modabi.data;

import java.lang.reflect.Method;
import java.util.List;

import uk.co.strangeskies.modabi.model.nodes.DataNode;
import uk.co.strangeskies.modabi.processing.BindingStrategy;
import uk.co.strangeskies.modabi.processing.UnbindingStrategy;

public interface DataType<T> {
	String getName();

	Class<T> getDataClass();

	Class<?> getBindingClass();

	BindingStrategy getBindingStrategy();

	Class<?> getUnbindingClass();

	UnbindingStrategy getUnbindingStrategy();

	Method getUnbindingMethod();

	String getUnbindingMethodName();

	List<DataNode<?>> getChildren();
}
