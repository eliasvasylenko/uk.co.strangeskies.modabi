package uk.co.strangeskies.modabi.impl.schema;

import java.util.List;
import java.util.function.Function;

import uk.co.strangeskies.modabi.schema.BindingCondition;
import uk.co.strangeskies.modabi.schema.ChildBindingPoint;
import uk.co.strangeskies.modabi.schema.ChildBindingPointConfigurator;
import uk.co.strangeskies.reflection.TypeParameter;
import uk.co.strangeskies.reflection.TypeToken;

public class ChildBindingPointImpl<T> extends BindingPointImpl<T> implements ChildBindingPoint<T> {
	private final boolean extensible;
	private final boolean ordered;
	private final BindingCondition<? super T> condition;
	private final List<T> values;

	protected ChildBindingPointImpl(ChildBindingPointConfiguratorImpl<T> configurator) {
		super(configurator);

		extensible = configurator
				.overrideChildren(ChildBindingPoint::extensible, ChildBindingPointConfigurator::getExtensible)
				.validateOverride((a, b) -> true)
				.get();

		ordered = configurator
				.overrideChildren(ChildBindingPoint::ordered, ChildBindingPointConfigurator::getOrdered)
				.validateOverride((a, b) -> a || !b)
				.get();

		condition = configurator
				.overrideChildren(
						(Function<ChildBindingPoint<T>, BindingCondition<? super T>>) ChildBindingPoint::bindingCondition,
						ChildBindingPointConfigurator::getBindingCondition)
				.get();
		
		values 
	}

	@Override
	public TypeToken<ChildBindingPoint<T>> getThisType() {
		return new TypeToken<ChildBindingPoint<T>>() {}.withTypeArgument(new TypeParameter<T>() {}, dataType());
	}

	@Override
	public boolean extensible() {
		return extensible;
	}

	@Override
	public boolean ordered() {
		return ordered;
	}

	@Override
	public BindingCondition<? super T> bindingCondition() {
		return condition;
	}

	@Override
	public List<T> providedValues() {
		return values;
	}
}
