package uk.co.strangeskies.modabi.schema;

import static uk.co.strangeskies.reflection.token.TypeToken.forClass;

import java.util.Optional;
import java.util.function.Function;

import uk.co.strangeskies.reflection.codegen.Expression;
import uk.co.strangeskies.reflection.codegen.ValueExpression;
import uk.co.strangeskies.reflection.token.TypeToken;

public interface ChildBindingPointConfigurator<T, E extends SchemaNodeConfigurator<?, ?>>
		extends BindingPointConfigurator<ChildBindingPointConfigurator<T, E>> {
	ChildBindingPointConfigurator<T, E> name(String name);

	ChildBindingPointConfigurator<T, E> extensible(boolean extensible);

	Optional<Boolean> getExtensible();

	ChildBindingPointConfigurator<T, E> ordered(boolean ordered);

	Optional<Boolean> getOrdered();

	ChildBindingPointConfigurator<T, E> bindingCondition(BindingCondition<? super T> condition);

	Optional<BindingCondition<? super T>> getBindingCondition();

	InputConfigurator<T> input();

	OutputConfigurator<T> output();

	default ChildBindingPointConfigurator<T, E> input(
			Function<InputConfigurator<T>, Expression> inputExpression) {
		input().expression(inputExpression.apply(input()));
		return this;
	}

	default ChildBindingPointConfigurator<T, E> output(
			Function<OutputConfigurator<T>, ValueExpression<T>> outputExpression) {
		output().expression(outputExpression.apply(output()));
		return this;
	}

	@Override
	default <V> SchemaNodeConfigurator<V, E> withNodeOfType(Class<V> dataType) {
		return withNodeOfType(forClass(dataType));
	}

	@Override
	<V> SchemaNodeConfigurator<V, E> withNodeOfType(TypeToken<V> dataType);

	@Override
	<V> E withoutNode(TypeToken<V> dataType);

	@Override
	default <V> E withoutNode(Class<V> dataType) {
		return withoutNode(forClass(dataType));
	}

	@Override
	default <V> SchemaNodeConfigurator<V, E> withNodeOfModel(Model<V> baseModel) {
		return withNodeOfType(baseModel.dataType()).baseModel(baseModel);
	}
}
