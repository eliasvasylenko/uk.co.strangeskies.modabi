package uk.co.strangeskies.modabi.impl.schema;

import uk.co.strangeskies.modabi.impl.schema.utilities.OverrideMerge;
import uk.co.strangeskies.modabi.schema.DataBindingType;
import uk.co.strangeskies.utilities.PropertySet;

public class DataBindingTypeImpl<T> extends
		BindingNodeImpl<T, DataBindingType<T>, DataBindingType.Effective<T>>
		implements DataBindingType<T> {
	private static class Effective<T>
			extends
			BindingNodeImpl.Effective<T, DataBindingType<T>, DataBindingType.Effective<T>>
			implements DataBindingType.Effective<T> {
		private final Boolean isPrivate;

		private final DataBindingType.Effective<? super T> baseType;

		public Effective(
				OverrideMerge<DataBindingType<T>, DataBindingTypeConfiguratorImpl<T>> overrideMerge) {
			super(overrideMerge);

			isPrivate = overrideMerge.node().isPrivate() != null
					&& overrideMerge.node().isPrivate();

			baseType = overrideMerge.configurator().getBaseType() == null ? null
					: overrideMerge.configurator().getBaseType().effective();
		}

		@Override
		public Boolean isPrivate() {
			return isPrivate;
		}

		@Override
		public DataBindingType.Effective<? super T> baseType() {
			return baseType;
		}

		@SuppressWarnings("rawtypes")
		protected static final PropertySet<DataBindingType.Effective> PROPERTY_SET = new PropertySet<>(
				DataBindingType.Effective.class)
				.add(BindingNodeImpl.Effective.PROPERTY_SET)
				.add(DataBindingTypeImpl.PROPERTY_SET).add(DataBindingType::isPrivate)
				.add(DataBindingType::baseType);

		@SuppressWarnings("unchecked")
		@Override
		protected PropertySet<DataBindingType.Effective<T>> effectivePropertySet() {
			return (PropertySet<DataBindingType.Effective<T>>) (Object) PROPERTY_SET;
		}
	}

	private final DataBindingTypeImpl.Effective<T> effective;

	private final Boolean isPrivate;

	private final DataBindingType<? super T> baseType;

	public DataBindingTypeImpl(DataBindingTypeConfiguratorImpl<T> configurator) {
		super(configurator);

		isPrivate = configurator.getIsPrivate();

		baseType = configurator.getBaseType();

		effective = new DataBindingTypeImpl.Effective<>(
				DataBindingTypeConfiguratorImpl.overrideMerge(this, configurator));
	}

	@SuppressWarnings("rawtypes")
	protected static final PropertySet<DataBindingType> PROPERTY_SET = new PropertySet<>(
			DataBindingType.class).add(BindingNodeImpl.PROPERTY_SET)
			.add(DataBindingType::isPrivate).add(DataBindingType::baseType);

	@SuppressWarnings("unchecked")
	@Override
	protected PropertySet<DataBindingType<T>> propertySet() {
		return (PropertySet<DataBindingType<T>>) (Object) PROPERTY_SET;
	}

	@Override
	public Boolean isPrivate() {
		return isPrivate;
	}

	@Override
	public DataBindingType<? super T> baseType() {
		return baseType;
	}

	@Override
	public DataBindingTypeImpl.Effective<T> effective() {
		return effective;
	}
}
