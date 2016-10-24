package uk.co.strangeskies.modabi.impl.schema;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import uk.co.strangeskies.modabi.impl.schema.old.InputProcess;
import uk.co.strangeskies.modabi.impl.schema.old.OutputProcess;
import uk.co.strangeskies.modabi.impl.schema.utilities.OverrideBuilder;
import uk.co.strangeskies.modabi.processing.ProcessingContext;
import uk.co.strangeskies.modabi.schema.BindingPoint;
import uk.co.strangeskies.modabi.schema.ChildBindingPoint;
import uk.co.strangeskies.modabi.schema.ChildBindingPointConfigurator;
import uk.co.strangeskies.modabi.schema.InputConfigurator;
import uk.co.strangeskies.modabi.schema.OutputConfigurator;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.reflection.codegen.Block;
import uk.co.strangeskies.reflection.codegen.ClassDeclaration;
import uk.co.strangeskies.reflection.codegen.ClassDefinition;
import uk.co.strangeskies.reflection.codegen.Expression;
import uk.co.strangeskies.reflection.codegen.MethodDeclaration;
import uk.co.strangeskies.reflection.codegen.ValueExpression;
import uk.co.strangeskies.reflection.codegen.VariableExpression;

public class ChildBindingPointConfiguratorImpl<T> extends
		BindingPointConfiguratorImpl<T, ChildBindingPointConfigurator<T>> implements ChildBindingPointConfigurator<T> {
	private static final AtomicLong COUNT = new AtomicLong();

	private final ClassDefinition<? extends InputProcess<T>> inputClass;
	private final Block<Object> inputBlock;
	private final VariableExpression<Object> inputTargetExpression;
	private final VariableExpression<T> inputResultExpression;
	private Expression inputExpression;

	private final ClassDefinition<? extends OutputProcess<T>> outputClass;
	private final Block<T> outputBlock;
	private final VariableExpression<Object> outputSourceExpression;
	private ValueExpression<T> outputExpression;

	@SuppressWarnings("unchecked")
	public ChildBindingPointConfiguratorImpl(ChildBindingPointConfigurator<T> other) {
		super(other);

		long count = COUNT.getAndIncrement();
	}

	public ChildBindingPointConfiguratorImpl() {
		long count = COUNT.getAndIncrement();

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
	}

	@Override
	public ChildBindingPointConfigurator<T> copy() {
		return new ChildBindingPointConfiguratorImpl<>(this);
	}

	@Override
	public ChildBindingPoint<T> create() {
		return (ChildBindingPoint<T>) super.create();
	}

	@Override
	protected BindingPoint<T> createImpl() {
		return new ChildBindingPointImpl<>(this);
	}

	@Override
	public InputConfigurator<T> input() {
		return new InputConfigurator<T>() {
			@Override
			public ValueExpression<T> result() {
				return inputResultExpression;
			}

			@Override
			public VariableExpression<Object> target() {
				return inputTargetExpression;
			}

			@Override
			public void expression(Expression expression) {
				inputExpression = expression;
				inputBlock.addExpression(expression);
			}

			@Override
			public <U> ValueExpression<U> provide(TypeToken<U> type) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public <U> ValueExpression<U> provideFor(BindingPoint<U> type) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public ValueExpression<ProcessingContext> context() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public ValueExpression<?> bound(String string) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public ValueExpression<?> provide() {
				return provide(getDataType());
			}
		};
	}

	@Override
	public OutputConfigurator<T> output() {
		return new OutputConfigurator<T>() {
			@Override
			public ValueExpression<Object> source() {
				return outputSourceExpression;
			}

			@Override
			public void expression(ValueExpression<T> expression) {
				outputExpression = expression;
				outputBlock.addReturnStatement(expression);
			}

			@Override
			public <U> ValueExpression<U> provideFor(BindingPoint<U> type) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public ValueExpression<ProcessingContext> context() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public ValueExpression<?> bound(String string) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public <U> ValueExpression<U> iterate(ValueExpression<Iterable<U>> values) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public ValueExpression<?> provide() {
				return provide(getDataType());
			}
		};
	}

	@Override
	protected Set<BindingPoint<T>> getOverriddenBindingPoints() {
		Set<BindingPoint<T>> bindingPoints = new HashSet<>(getOverriddenChildBindingPoints());
		bindingPoints.addAll(getBaseModel());
		return bindingPoints;
	}

	protected Set<ChildBindingPoint<T>> getOverriddenChildBindingPoints();

	public <U> OverrideBuilder<U, ChildBindingPoint<T>> overrideChildren(
			Function<? super ChildBindingPoint<T>, ? extends U> overriddenValues,
			Function<? super ChildBindingPointConfigurator<T>, ? extends U> overridingValue) {
		return new OverrideBuilder<>(getOverriddenChildBindingPoints(), overriddenValues, overridingValue.apply(this));
	}
}
