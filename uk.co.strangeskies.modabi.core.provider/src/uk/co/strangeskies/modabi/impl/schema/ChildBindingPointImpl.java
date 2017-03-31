package uk.co.strangeskies.modabi.impl.schema;

import java.util.List;
import java.util.stream.Stream;

import uk.co.strangeskies.modabi.ModabiException;
import uk.co.strangeskies.modabi.ValueResolution;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.schema.BindingCondition;
import uk.co.strangeskies.modabi.schema.ChildBindingPoint;
import uk.co.strangeskies.modabi.schema.ChildBindingPointConfigurator;

public class ChildBindingPointImpl<T> extends BindingPointImpl<T> implements ChildBindingPoint<T> {
	private final boolean extensible;
	private final boolean ordered;
	private final BindingCondition<? super T> condition;

	private final ValueResolution resolution;
	private final DataSource valuesBuffer;
	private final List<T> values;

	@SuppressWarnings("unchecked")
	protected ChildBindingPointImpl(ChildBindingPointConfiguratorImpl<T> configurator) {
		super(configurator);

		extensible = configurator
				.overrideChildren(
						ChildBindingPoint::extensible,
						ChildBindingPointConfigurator::getExtensible)
				.validateOverride((a, b) -> true)
				.orDefault(false)
				.get();

		ordered = configurator
				.overrideChildren(ChildBindingPoint::ordered, ChildBindingPointConfigurator::getOrdered)
				.validateOverride((a, b) -> a || !b)
				.get();

		BindingCondition<?> condition = configurator
				.<BindingCondition<?>>overrideChildren(
						ChildBindingPoint::bindingCondition,
						ChildBindingPointConfigurator::getBindingCondition)
				.get();
		this.condition = (BindingCondition<? super T>) condition;

		valuesBuffer = configurator
				.overrideChildren(
						ChildBindingPoint::providedValuesBuffer,
						ChildBindingPointConfigurator::getProvidedValue)
				.tryGet();

		resolution = configurator
				.overrideChildren(
						ChildBindingPoint::providedValuesResolution,
						ChildBindingPointConfigurator::getValueResolution)
				.validateOverride(
						(o, n) -> o == n
								|| (o == ValueResolution.DECLARATION_TIME && n == ValueResolution.POST_DECLARATION))
				.orDefault(ValueResolution.PROCESSING_TIME)
				.get();

		if (valuesBuffer == null
				&& concrete()
				&& (resolution == ValueResolution.DECLARATION_TIME
						|| resolution == ValueResolution.POST_DECLARATION))
			throw new ModabiException(
					"Value must be provided at registration time for node '" + name() + "'");

		if ((resolution == ValueResolution.DECLARATION_TIME
				|| resolution == ValueResolution.POST_DECLARATION) && valuesBuffer != null) {
			/*
			 * TODO The previous approach taken here was all wrong. The process of
			 * binding should essentially allow us to determine the type as a free
			 * side-effect! The exact type is inferred as we go along in such cases.
			 * Just propagate this information out through the API to here.
			 * 
			 * We still need a special (perhaps internal?) DataLoader api here rather
			 * than just passing through the source SchemaManager directly, as
			 * BaseSchema and MetaSchema are built before the SchemaManager. They need
			 * their results spoofing since they are built before the normal binding
			 * process and models can be available.
			 * 
			 * TODO 2 generalize providing these values to allow buffering structured
			 * data, too. i.e. StructuredDataSource instead of DataSource
			 */
			values = null;
			// configurator.dataLoader().loadData(this, valuesBuffer);
		} else {
			values = null;
		}
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
	public Stream<T> providedValues() {
		return values.stream();
	}

	@Override
	public ValueResolution providedValuesResolution() {
		return resolution;
	}

	@Override
	public DataSource providedValuesBuffer() {
		return valuesBuffer;
	}
}
