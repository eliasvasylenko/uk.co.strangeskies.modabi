package uk.co.strangeskies.modabi.impl.schema;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static uk.co.strangeskies.reflection.codegen.ClassSignature.classSignature;
import static uk.co.strangeskies.reflection.codegen.ParameterSignature.parameterSignature;
import static uk.co.strangeskies.reflection.token.TypeToken.forNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import uk.co.strangeskies.modabi.ValueResolution;
import uk.co.strangeskies.modabi.impl.schema.utilities.ChildBindingPointConfigurationContext;
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
import uk.co.strangeskies.reflection.Methods;
import uk.co.strangeskies.reflection.codegen.ClassDefinition;
import uk.co.strangeskies.reflection.codegen.Expression;
import uk.co.strangeskies.reflection.codegen.ExpressionVisitor.ValueExpressionVisitor;
import uk.co.strangeskies.reflection.codegen.MethodDeclaration;
import uk.co.strangeskies.reflection.codegen.ValueExpression;
import uk.co.strangeskies.reflection.codegen.VariableExpression;
import uk.co.strangeskies.reflection.token.TypeToken;

public class ChildBindingPointConfiguratorImpl<T>
		extends BindingPointConfiguratorImpl<T, ChildBindingPointConfigurator<T>>
		implements ChildBindingPointConfigurator<T> {
	private static class NoneExpression<U> implements ValueExpression<U> {
		@Override
		public void accept(ValueExpressionVisitor<U> visitor) {
			throw new UnsupportedOperationException();
		}

		@SuppressWarnings("unchecked")
		@Override
		public TypeToken<U> getType() {
			return (TypeToken<U>) forNull();
		}
	}

	private final ChildBindingPointConfigurationContext context;

	private Boolean extensible;

	private Expression inputExpression;
	private ValueExpression<? extends T> outputExpression;

	private BindingCondition<? super T> bindingCondition;
	private Boolean ordered;

	private DataSource providedValue;
	private ValueResolution valueResolution;

	/*
	 * A mapping from placeholder iteration item expressions to the iterable
	 * expressions they come from.
	 */
	private final Map<ValueExpression<?>, ValueExpression<? extends Iterable<?>>> iterationExpressions;

	@SuppressWarnings("unchecked")
	public ChildBindingPointConfiguratorImpl(ChildBindingPointConfigurator<T> other) {
		super(other);

		context = null;

		iterationExpressions = new HashMap<>();
	}

	public ChildBindingPointConfiguratorImpl(ChildBindingPointConfigurationContext context) {
		this.context = context;

		/*
		 * output
		 */
		iterationExpressions = new HashMap<>();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Stream<BindingPoint<?>> getOverriddenBindingPoints() {
		return concat(
				getOverriddenChildBindingPoints(),
				(Stream<? extends BindingPoint<T>>) getBaseModel());
	}

	protected Stream<ChildBindingPoint<?>> getOverriddenChildBindingPoints() {
		return getName().map(context::overrideChild).orElse(Stream.empty());
	}

	public <U> OverrideBuilder<U> overrideChildren(
			Function<? super ChildBindingPoint<?>, ? extends U> overriddenValues,
			Function<? super ChildBindingPointConfigurator<T>, Optional<? extends U>> overridingValue) {
		return new OverrideBuilder<>(
				getOverriddenChildBindingPoints().map(overriddenValues::apply).collect(toList()),
				overridingValue.apply(this),
				() -> Methods.findMethod(ChildBindingPoint.class, overriddenValues::apply).getName());
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
		ClassDefinition<Void, ? extends InputProcess> inputClass = classSignature()
				.withSuperType(InputProcess.class)
				.defineStandalone();

		MethodDeclaration<? extends InputProcess, ?> inputMethod = inputClass
				.getDeclaration()
				.getMethodDeclaration("process", ProcessingContext.class, Object.class, Object.class);

		ValueExpression<ProcessingContext> contextExpression = inputMethod
				.getParameter(parameterSignature("context", ProcessingContext.class));
		VariableExpression<Object> inputTargetExpression = inputMethod
				.getParameter(parameterSignature("inputTarget", Object.class));
		VariableExpression<Object> resultExpression = inputMethod
				.getParameter(parameterSignature("result", Object.class));

		return new InputConfigurator<T>() {
			@Override
			public ValueExpression<? extends T> result() {
				return resultExpression;
			}

			@Override
			public VariableExpression<Object> target() {
				return inputTargetExpression;
			}

			@Override
			public void expression(Expression expression) {
				inputExpression = expression;
			}

			@Override
			public <U> ValueExpression<U> provideFor(BindingPoint<U> type) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public ValueExpression<ProcessingContext> context() {
				return contextExpression;
			}

			@Override
			public ValueExpression<?> bound(String string) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public ValueExpression<?> provide() {
				return provide(getDataType().get());
			}

			@Override
			public Expression getExpression() {
				return inputExpression;
			}

			@Override
			public <U> ValueExpression<U> none() {
				return new NoneExpression<>();
			}

			@Override
			public <U> ValueExpression<U> iterate(ValueExpression<? extends Iterable<U>> values) {
				// TODO Auto-generated method stub
				return null;
			}
		};
	}

	@Override
	public OutputConfigurator<T> output() {
		/*
		 * input
		 */
		ClassDefinition<Void, ? extends OutputProcess> outputClass = classSignature()
				.withSuperType(OutputProcess.class)
				.defineStandalone();

		MethodDeclaration<? extends OutputProcess, ?> outputMethod = outputClass
				.getDeclaration()
				.getMethodDeclaration("process", ProcessingContext.class, Object.class);

		ValueExpression<ProcessingContext> contextExpression = outputMethod
				.getParameter(parameterSignature("context", ProcessingContext.class));
		ValueExpression<Object> outputSourceExpression = outputMethod
				.getParameter(parameterSignature("outputSource", Object.class));

		return new OutputConfigurator<T>() {
			@Override
			public ValueExpression<Object> source() {
				return outputSourceExpression;
			}

			@Override
			public void expression(ValueExpression<? extends T> expression) {
				outputExpression = expression;
			}

			@Override
			public <U> ValueExpression<U> provideFor(BindingPoint<U> type) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public ValueExpression<ProcessingContext> context() {
				return contextExpression;
			}

			@Override
			public ValueExpression<?> bound(String string) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public <U> ValueExpression<U> iterate(ValueExpression<? extends Iterable<U>> values) {
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

			@Override
			public <U> ValueExpression<U> none() {
				return new NoneExpression<>();
			}
		};
	}

	@Override
	public final ChildBindingPointConfigurator<T> name(String name) {
		return name(name, context.namespace());
	}

	@Override
	public ChildBindingPointConfigurator<T> ordered(boolean ordered) {
		this.ordered = ordered;
		return this;
	}

	@Override
	public Optional<Boolean> getOrdered() {
		return ofNullable(ordered);
	}

	@Override
	public ChildBindingPointConfigurator<T> valueResolution(ValueResolution valueResolution) {
		this.valueResolution = valueResolution;
		return this;
	}

	@Override
	public Optional<ValueResolution> getValueResolution() {
		return ofNullable(valueResolution);
	}

	@Override
	public ChildBindingPointConfigurator<T> provideValue(DataSource buffer) {
		this.providedValue = buffer;
		return this;
	}

	@Override
	public Optional<DataSource> getProvidedValue() {
		return ofNullable(providedValue);
	}

	@Override
	public ChildBindingPointConfigurator<T> extensible(boolean extensible) {
		this.extensible = extensible;
		return this;
	}

	@Override
	public Optional<Boolean> getExtensible() {
		return ofNullable(extensible);
	}

	@Override
	public ChildBindingPointConfigurator<T> bindingCondition(BindingCondition<? super T> condition) {
		this.bindingCondition = condition;
		return this;
	}

	@Override
	public Optional<BindingCondition<? super T>> getBindingCondition() {
		return ofNullable(bindingCondition);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V> ChildBindingPointConfigurator<V> baseModel(Model<? super V> baseModel) {
		return (ChildBindingPointConfigurator<V>) super.baseModel(baseModel);
	}

	@Override
	public ChildBindingPointConfigurator<?> baseModel(Collection<? extends Model<?>> baseModel) {
		return (ChildBindingPointConfigurator<?>) super.baseModel(baseModel);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V> ChildBindingPointConfigurator<V> dataType(TypeToken<V> dataType) {
		return (ChildBindingPointConfigurator<V>) super.dataType(dataType);
	}
}
