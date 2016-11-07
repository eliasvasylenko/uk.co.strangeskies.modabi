package uk.co.strangeskies.modabi.impl.schema;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import uk.co.strangeskies.modabi.ValueResolution;
import uk.co.strangeskies.modabi.impl.schema.utilities.OverrideBuilder;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.processing.ProcessingContext;
import uk.co.strangeskies.modabi.schema.BindingCondition;
import uk.co.strangeskies.modabi.schema.BindingPoint;
import uk.co.strangeskies.modabi.schema.ChildBindingPoint;
import uk.co.strangeskies.modabi.schema.ChildBindingPointConfigurator;
import uk.co.strangeskies.modabi.schema.InputConfigurator;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.OutputConfigurator;
import uk.co.strangeskies.reflection.codegen.Block;
import uk.co.strangeskies.reflection.codegen.ClassDeclaration;
import uk.co.strangeskies.reflection.codegen.ClassDefinition;
import uk.co.strangeskies.reflection.codegen.Expression;
import uk.co.strangeskies.reflection.codegen.MethodDeclaration;
import uk.co.strangeskies.reflection.codegen.ValueExpression;
import uk.co.strangeskies.reflection.codegen.VariableExpression;
import uk.co.strangeskies.reflection.token.TypeToken;

public class ChildBindingPointConfiguratorImpl<T> extends
		BindingPointConfiguratorImpl<T, ChildBindingPointConfigurator<T>> implements ChildBindingPointConfigurator<T> {
	private static final AtomicLong COUNT = new AtomicLong();

	private Expression inputExpression;

	private final ClassDefinition<? extends OutputProcess<T>> outputClass;
	private final Block<T> outputBlock;
	private final VariableExpression<Object> outputSourceExpression;
	private ValueExpression<? extends T> outputExpression;

	@SuppressWarnings("unchecked")
	public ChildBindingPointConfiguratorImpl(ChildBindingPointConfigurator<T> other) {
		super(other);

		long count = COUNT.getAndIncrement();
	}

	public ChildBindingPointConfiguratorImpl() {
		long count = COUNT.getAndIncrement();

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

	@SuppressWarnings("unchecked")
	@Override
	protected Set<BindingPoint<T>> getOverriddenBindingPoints() {
		Set<BindingPoint<T>> bindingPoints = new HashSet<>(getOverriddenChildBindingPoints());
		bindingPoints.addAll((Collection<? extends BindingPoint<T>>) getBaseModel());
		return bindingPoints;
	}

	protected Set<ChildBindingPoint<T>> getOverriddenChildBindingPoints();

	public <U> OverrideBuilder<U, ChildBindingPoint<T>> overrideChildren(
			Function<? super ChildBindingPoint<T>, ? extends U> overriddenValues,
			Function<? super ChildBindingPointConfigurator<T>, ? extends U> overridingValue) {
		return new OverrideBuilder<>(getOverriddenChildBindingPoints(), overriddenValues, overridingValue.apply(this));
	}

	@Override
	public ChildBindingPointConfigurator<T> copy() {
		return new ChildBindingPointConfiguratorImpl<>(this);
	}

	@Override
	public ChildBindingPoint<T> create() {
		return new ChildBindingPointImpl<>(this);
	}

	@Override
	public InputConfigurator<T> input() {
		/*
		 * input
		 */
		ClassDefinition<? extends InputProcess<T>> inputClass = ClassDeclaration
				.declareClass(getClass().getName() + "$" + InputProcess.class.getSimpleName() + count)
				.withSuperType(new TypeToken<InputProcess<T>>() {})
				.define();

		MethodDeclaration<?, ?> inputMethod = inputClass.declareMethodOverride(i -> i.process(null, null));

		VariableExpression<Object> inputTargetExpression = inputMethod.addParameter(Object.class);
		VariableExpression<T> inputResultExpression = inputMethod.addParameter(new TypeToken<T>() {});

		Block<Object> inputBlock = inputMethod.withReturnType(Object.class).define().body();

		return new InputConfigurator<T>() {
			@Override
			public ValueExpression<? extends T> result() {
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

			@Override
			public Expression getExpression() {
				return inputExpression;
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
			public void expression(ValueExpression<? extends T> expression) {
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

			@Override
			public ValueExpression<? extends T> getExpression() {
				return outputExpression;
			}
		};
	}

	@Override
	public ChildBindingPointConfigurator<T> ordered(boolean ordered) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean getOrdered() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ChildBindingPointConfigurator<T> valueResolution(ValueResolution registrationTime) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ValueResolution getValueResolution() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ChildBindingPointConfigurator<T> provideValue(DataSource buffer) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataSource getProvidedValue() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ChildBindingPointConfigurator<T> extensible(boolean extensible) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean getExtensible() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ChildBindingPointConfigurator<T> bindingCondition(BindingCondition<? super T> condition) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BindingCondition<? super T> getBindingCondition() {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V> ChildBindingPointConfigurator<V> baseModel(Model<? super V> baseModel) {
		return (ChildBindingPointConfigurator<V>) super.baseModel(baseModel);
	}
}
