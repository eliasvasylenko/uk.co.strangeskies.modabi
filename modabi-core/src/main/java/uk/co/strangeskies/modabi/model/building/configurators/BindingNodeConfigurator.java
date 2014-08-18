package uk.co.strangeskies.modabi.model.building.configurators;

import java.util.Arrays;
import java.util.List;

import uk.co.strangeskies.modabi.model.nodes.BindingChildNode;
import uk.co.strangeskies.modabi.model.nodes.BindingNode;
import uk.co.strangeskies.modabi.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.processing.BindingStrategy;
import uk.co.strangeskies.modabi.schema.processing.UnbindingStrategy;

public interface BindingNodeConfigurator<S extends BindingNodeConfigurator<S, N, T, C, B>, N extends BindingNode<T, ?, ?>, T, C extends ChildNode<?, ?>, B extends BindingChildNode<?, ?, ?>>
		extends SchemaNodeConfigurator<S, N>, BranchingNodeConfigurator<S, N, C, B> {
	<V extends T> BindingNodeConfigurator<?, ?, V, C, B> dataClass(
			Class<V> dataClass);

	S bindingStrategy(BindingStrategy strategy);

	S bindingClass(Class<?> bindingClass);

	S unbindingStrategy(UnbindingStrategy strategy);

	S unbindingFactoryClass(Class<?> factoryClass);

	S unbindingClass(Class<?> unbindingClass);

	S unbindingMethod(String unbindingMethod);

	S providedUnbindingParameters(List<QualifiedName> parameterNames);

	default S providedUnbindingParameters(QualifiedName... parameterNames) {
		return providedUnbindingParameters(Arrays.asList(parameterNames));
	}

	S providedUnbindingParameters(String... parameterNames);
}