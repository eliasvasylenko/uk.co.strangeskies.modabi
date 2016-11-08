package uk.co.strangeskies.modabi.impl.schema;

import static uk.co.strangeskies.reflection.token.TypeToken.overNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
import uk.co.strangeskies.reflection.codegen.Expression;
import uk.co.strangeskies.reflection.codegen.ExpressionVisitor.ValueExpressionVisitor;
import uk.co.strangeskies.reflection.codegen.ValueExpression;
import uk.co.strangeskies.reflection.codegen.VariableExpression;
import uk.co.strangeskies.reflection.token.TypeToken;

public class ChildBindingPointConfiguratorImpl<T> extends
		BindingPointConfiguratorImpl<T, ChildBindingPointConfigurator<T>> implements ChildBindingPointConfigurator<T> {
	private static class NoneExpression<U> implements ValueExpression<U> {
		@Override
		public void accept(ValueExpressionVisitor<U> visitor) {
			throw new UnsupportedOperationException();
		}

		@Override
		public TypeToken<U> getType() {
			return overNull();
		}
	}

	private Boolean extensible;

	private Expression inputExpression;
	private ValueExpression<? extends T> outputExpression;
	private final VariableExpression<Object> outputSourcePlaceholderExpression;

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

		iterationExpressions = new HashMap<>();
	}

	public ChildBindingPointConfiguratorImpl() {
		/*
		 * output
		 */
		outputSourcePlaceholderExpression = null;// TODO placeholder expression

		iterationExpressions = new HashMap<>();
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
		VariableExpression<Object> inputTargetExpression = inputMethod.addParameter(Object.class);
		VariableExpression<T> inputResultExpression = inputMethod.addParameter(new TypeToken<T>() {});

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

			@Override
			public <U> ValueExpression<U> none() {
				return new NoneExpression<>();
			}
		};
	}

	@Override
	public OutputConfigurator<T> output() {
		return new OutputConfigurator<T>() {
			@Override
			public ValueExpression<Object> source() {
				return outputSourcePlaceholderExpression;
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
				// TODO Auto-generated method stub
				return null;
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
		return name(name, null /* parent namespace */);
	}

	@Override
	public ChildBindingPointConfigurator<T> ordered(boolean ordered) {
		this.ordered = ordered;
		return this;
	}

	@Override
	public Boolean getOrdered() {
		return ordered;
	}

	@Override
	public ChildBindingPointConfigurator<T> valueResolution(ValueResolution valueResolution) {
		this.valueResolution = valueResolution;
		return this;
	}

	@Override
	public ValueResolution getValueResolution() {
		return valueResolution;
	}

	@Override
	public ChildBindingPointConfigurator<T> provideValue(DataSource buffer) {
		this.providedValue = buffer;
		return this;
	}

	@Override
	public DataSource getProvidedValue() {
		return providedValue;
	}

	@Override
	public ChildBindingPointConfigurator<T> extensible(boolean extensible) {
		this.extensible = extensible;
		return this;
	}

	@Override
	public Boolean getExtensible() {
		return extensible;
	}

	@Override
	public ChildBindingPointConfigurator<T> bindingCondition(BindingCondition<? super T> condition) {
		this.bindingCondition = condition;
		return this;
	}

	@Override
	public BindingCondition<? super T> getBindingCondition() {
		return bindingCondition;
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
}
