package uk.co.strangeskies.modabi.schema.node.building.configuration;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.management.binding.BindingStrategy;
import uk.co.strangeskies.modabi.schema.management.unbinding.UnbindingStrategy;
import uk.co.strangeskies.modabi.schema.node.BindingNode;
import uk.co.strangeskies.reflection.TypeLiteral;

public interface BindingNodeConfigurator<S extends BindingNodeConfigurator<S, N, T>, N extends BindingNode<? extends T, ?, ?>, T>
		extends SchemaNodeConfigurator<S, N> {
	default <V extends T> BindingNodeConfigurator<?, ?, V> dataClass(
			Class<V> dataClass) {
		return (BindingNodeConfigurator<?, ?, V>) dataType(new TypeLiteral<>(
				dataClass));
	}

	<V extends T> BindingNodeConfigurator<?, ?, V> dataType(
			TypeLiteral<V> dataType);

	S bindingStrategy(BindingStrategy strategy);

	S bindingType(Type bindingType);

	S unbindingStrategy(UnbindingStrategy strategy);

	S unbindingFactoryType(Type factoryType);

	S unbindingType(Type unbindingType);

	S unbindingMethod(String unbindingMethod);

	S providedUnbindingMethodParameters(List<QualifiedName> parameterNames);

	default S providedUnbindingMethodParameters(QualifiedName... parameterNames) {
		return providedUnbindingMethodParameters(Arrays.asList(parameterNames));
	}

	S providedUnbindingMethodParameters(String... parameterNames);
}