package uk.co.strangeskies.modabi.impl.schema;

import static java.util.stream.Collectors.toList;
import static uk.co.strangeskies.reflection.ConstraintFormula.Kind.EQUALITY;
import static uk.co.strangeskies.reflection.ConstraintFormula.Kind.SUBTYPE;

import java.util.List;
import java.util.stream.Stream;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.schema.BindingPoint;
import uk.co.strangeskies.modabi.schema.BindingPointConfigurator;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.SchemaNode;
import uk.co.strangeskies.reflection.Types;
import uk.co.strangeskies.reflection.token.TypeToken;

public abstract class BindingPointImpl<T> implements BindingPoint<T> {
	private final QualifiedName name;
	private final boolean concrete;
	private final TypeToken<T> dataType;
	private final SchemaNode node;
	private final List<Model<?>> baseModel;

	@SuppressWarnings("unchecked")
	protected BindingPointImpl(BindingPointConfiguratorImpl<T, ?> configurator) {
		name = configurator
				.override(BindingPoint::name, BindingPointConfigurator::getName)
				.validateOverride((a, b) -> true)
				.get();

		concrete = configurator.getConcrete().orElse(true);

		TypeToken<?> dataType = configurator
				.override(BindingPoint::dataType, BindingPointConfigurator::getDataType)
				.orMerged((o, n) -> o.withConstraintTo(EQUALITY, n))
				.mergeOverride((o, b) -> mergeOverriddenTypes(o, b))
				.get();
		this.dataType = (TypeToken<T>) dataType;

		node = configurator.getNode();

		baseModel = configurator.getBaseModel().collect(toList());
	}

	private static <T> TypeToken<T> mergeOverriddenTypes(TypeToken<T> override, TypeToken<?> base) {
		if (!base.isProper() || !Types.isAssignable(override.getType(), base.getType())) {
			return override.withConstraintTo(SUBTYPE, base.deepCopy());
		} else {
			return override;
		}
	}

	@Override
	public QualifiedName name() {
		return name;
	}

	@Override
	public boolean concrete() {
		return concrete;
	}

	@Override
	public TypeToken<T> dataType() {
		return dataType;
	}

	@Override
	public SchemaNode node() {
		return node;
	}

	@Override
	public Stream<Model<?>> baseModel() {
		return baseModel.stream();
	}
}
