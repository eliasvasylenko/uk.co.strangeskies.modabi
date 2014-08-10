package uk.co.strangeskies.modabi.model.nodes;

import java.lang.reflect.Method;
import java.util.List;

import uk.co.strangeskies.modabi.schema.processing.BindingStrategy;
import uk.co.strangeskies.modabi.schema.processing.UnbindingStrategy;

public interface BindingNode<T, E extends BindingNode.Effective<T, E>> extends
		SchemaNode<E> {
	interface Effective<T, E extends Effective<T, E>> extends BindingNode<T, E>,
			SchemaNode.Effective<E> {
		Method getUnbindingMethod();

		List<DataNode.Effective<?>> getProvidedUnbindingMethodParameters();
	}

	Class<T> getDataClass();

	BindingStrategy getBindingStrategy();

	Class<?> getBindingClass();

	UnbindingStrategy getUnbindingStrategy();

	Class<?> getUnbindingClass();

	String getUnbindingMethodName();

	Class<?> getUnbindingFactoryClass();

	List<String> getProvidedUnbindingMethodParameterNames();
}
