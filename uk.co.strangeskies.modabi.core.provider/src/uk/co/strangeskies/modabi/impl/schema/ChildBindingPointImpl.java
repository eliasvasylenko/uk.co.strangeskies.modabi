package uk.co.strangeskies.modabi.impl.schema;

import java.util.List;
import java.util.function.Function;

import uk.co.strangeskies.modabi.schema.BindingCondition;
import uk.co.strangeskies.modabi.schema.ChildBindingPoint;
import uk.co.strangeskies.modabi.schema.ChildBindingPointConfigurator;
import uk.co.strangeskies.reflection.TypeParameter;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.reflection.codegen.ClassDeclaration;
import uk.co.strangeskies.reflection.codegen.MethodDeclaration;

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
		

		/*
		 * input
		 */
		inputClass = ClassDeclaration
				.declareClass(getClass().getName() + "$" + InputProcess.class.getSimpleName() + count)
				.withSuperType(new TypeToken<InputProcess<T>>() {})
				.define();

		MethodDeclaration<?, ?> inputMethod = inputClass.declareMethodOverride(i -> i.process(null, null));

		inputTargetExpression = inputMethod.addParameter(Object.class);
		inputResultExpression = inputMethod.addParameter(new TypeToken<T>() {});
		inputBlock = inputMethod.withReturnType(Object.class).define().body();

		/*
		 * output
		 */
		outputClass = ClassDeclaration
				.declareClass(getClass().getName() + "$" + OutputProcess.class.getSimpleName() + count)
				.withSuperType(new TypeToken<OutputProcess<T>>() {})
				.define();

		MethodDeclaration<?, ?> outputMethod = outputClass.declareMethodOverride(i -> i.process(null));

		outputSourceExpression = outputMethod.addParameter(Object.class);
		outputBlock = outputMethod.withReturnType(new TypeToken<T>() {}).define().body();
		
		values = null;
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
