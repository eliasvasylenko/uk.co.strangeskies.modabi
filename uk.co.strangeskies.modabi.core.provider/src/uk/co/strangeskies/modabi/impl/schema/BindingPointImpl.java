package uk.co.strangeskies.modabi.impl.schema;

import java.util.List;

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
	private final List<Model<? super T>> baseModel;

	@SuppressWarnings("unchecked")
	protected BindingPointImpl(BindingPointConfiguratorImpl<T, ?> configurator) {
		name = configurator.override(BindingPoint::name, BindingPointConfigurator::getName).get();

		concrete = configurator.getConcrete() == null || configurator.getConcrete();

		dataType = configurator
				.override(BindingPoint::dataType, BindingPointConfigurator::getDataType)
				.orMerged((o, n) -> o.withEquality(n))
				.mergeOverride((o, b) -> mergeOverriddenTypes(o, b))
				.get();

		node = configurator.getNode();

		baseModel = (List<Model<? super T>>) (List<?>) configurator.getBaseModel();
	}

	private static <T> TypeToken<T> mergeOverriddenTypes(TypeToken<T> override, TypeToken<?> base) {
		if (!base.isProper() || !Types.isAssignable(override.getType(), base.getType())) {
			return override.withUpperBound(base.deepCopy());
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
	public List<Model<? super T>> baseModel() {
		return baseModel;
	}
}
