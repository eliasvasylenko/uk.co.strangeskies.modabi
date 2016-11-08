package uk.co.strangeskies.modabi.schema;

import static java.util.Arrays.asList;
import static uk.co.strangeskies.reflection.token.TypeToken.overAnnotatedType;
import static uk.co.strangeskies.reflection.token.TypeToken.overType;

import java.lang.reflect.AnnotatedType;
import java.util.Collection;
import java.util.function.Function;

import uk.co.strangeskies.modabi.ValueResolution;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.reflection.codegen.Expression;
import uk.co.strangeskies.reflection.codegen.ValueExpression;
import uk.co.strangeskies.reflection.token.TypeToken;

public interface ChildBindingPointConfigurator<T>
		extends BindingPointConfigurator<T, ChildBindingPointConfigurator<T>> {
	ChildBindingPointConfigurator<T> name(String name);

	ChildBindingPointConfigurator<T> extensible(boolean extensible);

	Boolean getExtensible();

	ChildBindingPointConfigurator<T> ordered(boolean ordered);

	Boolean getOrdered();

	ChildBindingPointConfigurator<T> bindingCondition(BindingCondition<? super T> condition);

	BindingCondition<? super T> getBindingCondition();

	InputConfigurator<T> input();

	OutputConfigurator<T> output();

	default ChildBindingPointConfigurator<T> input(Function<InputConfigurator<T>, Expression> inputExpression) {
		input().expression(inputExpression.apply(input()));
		return this;
	}

	default ChildBindingPointConfigurator<T> output(
			Function<OutputConfigurator<T>, ValueExpression<T>> outputExpression) {
		output().expression(outputExpression.apply(output()));
		return this;
	}

	@Override
	default <V> ChildBindingPointConfigurator<V> dataType(Class<V> dataType) {
		return dataType(overType(dataType));
	}

	@Override
	<V> ChildBindingPointConfigurator<V> dataType(TypeToken<? super V> dataType);

	@Override
	default ChildBindingPointConfigurator<?> dataType(AnnotatedType dataType) {
		return dataType(overAnnotatedType(dataType));
	}

	@SuppressWarnings("unchecked")
	@Override
	default <V> ChildBindingPointConfigurator<V> baseModel(Model<? super V> baseModel) {
		return (ChildBindingPointConfigurator<V>) baseModel(asList(baseModel));
	}

	@Override
	ChildBindingPointConfigurator<?> baseModel(Collection<? extends Model<?>> baseModel);

	@Override
	ChildBindingPoint<T> create();

	ChildBindingPointConfigurator<T> valueResolution(ValueResolution registrationTime);

	ValueResolution getValueResolution();

	ChildBindingPointConfigurator<T> provideValue(DataSource buffer);

	DataSource getProvidedValue();
}
