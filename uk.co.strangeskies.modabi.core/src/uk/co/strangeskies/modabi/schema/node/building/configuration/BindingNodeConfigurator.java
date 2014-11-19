package uk.co.strangeskies.modabi.schema.node.building.configuration;

import java.util.Arrays;
import java.util.List;

import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.management.binding.BindingStrategy;
import uk.co.strangeskies.modabi.schema.management.unbinding.UnbindingStrategy;
import uk.co.strangeskies.modabi.schema.node.BindingNode;

public interface BindingNodeConfigurator<S extends BindingNodeConfigurator<S, N, T>, N extends BindingNode<? extends T, ?, ?>, T>
		extends SchemaNodeConfigurator<S, N> {
	<V extends T> BindingNodeConfigurator<?, ?, V> dataClass(Class<V> dataClass);

	S bindingStrategy(BindingStrategy strategy);

	S bindingClass(Class<?> bindingClass);

	S unbindingStrategy(UnbindingStrategy strategy);

	S unbindingFactoryClass(Class<?> factoryClass);

	S unbindingClass(Class<?> unbindingClass);

	S unbindingMethod(String unbindingMethod);

	S providedUnbindingMethodParameters(List<QualifiedName> parameterNames);

	default S providedUnbindingMethodParameters(QualifiedName... parameterNames) {
		return providedUnbindingMethodParameters(Arrays.asList(parameterNames));
	}

	S providedUnbindingMethodParameters(String... parameterNames);
}