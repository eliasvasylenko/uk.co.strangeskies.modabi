package uk.co.strangeskies.modabi.schema;

import static java.util.Arrays.asList;
import static uk.co.strangeskies.reflection.TypeToken.overAnnotatedType;
import static uk.co.strangeskies.reflection.TypeToken.overType;

import java.lang.reflect.AnnotatedType;
import java.util.Collection;
import java.util.function.Function;

import uk.co.strangeskies.modabi.ValueResolution;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.reflection.codegen.Expression;
import uk.co.strangeskies.reflection.codegen.ValueExpression;

public interface ChildBindingPointConfigurator<T>
		extends BindingPointConfigurator<T, ChildBindingPointConfigurator<T>> {
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

	ChildBindingPointConfigurator<T> noInput();

	ChildBindingPointConfigurator<T> noOutput();

	default ChildBindingPointConfigurator<T> noIO() {
		return noInput().noOutput();
	}

	@Override
	default <V> ChildBindingPointConfigurator<V> dataType(Class<V> dataType) {
		return dataType(overType(dataType));
	}

	@Override
	<V> ChildBindingPointConfigurator<V> dataType(TypeToken<? extends V> dataType);

	@Override
	default ChildBindingPointConfigurator<?> dataType(AnnotatedType dataType) {
		return dataType(overAnnotatedType(dataType));
	}

	@Override
	default <V> ChildBindingPointConfigurator<V> baseModel(Model<? extends V> baseModel) {
		return baseModel(asList(baseModel));
	}

	@Override
	<V> ChildBindingPointConfigurator<V> baseModel(Collection<? extends Model<? extends V>> baseModel);

	@Override
	ChildBindingPoint<T> create();

	ChildBindingPointConfigurator<T> valueResolution(ValueResolution registrationTime);

	ValueResolution getValueResolution();

	ChildBindingPointConfigurator<T> provideValue(DataSource buffer);

	DataSource getProvidedValue();
}
