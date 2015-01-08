package uk.co.strangeskies.modabi.schema.node.type.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.common.reflect.TypeToken;

import uk.co.strangeskies.modabi.namespace.Namespace;
import uk.co.strangeskies.reflection.TypeLiteral;
import uk.co.strangeskies.modabi.schema.node.building.DataLoader;
import uk.co.strangeskies.modabi.schema.node.building.configuration.impl.BindingNodeConfiguratorImpl;
import uk.co.strangeskies.modabi.schema.node.building.configuration.impl.utilities.OverrideMerge;
import uk.co.strangeskies.modabi.schema.node.type.DataBindingType;
import uk.co.strangeskies.modabi.schema.node.type.DataBindingTypeConfigurator;

public class DataBindingTypeConfiguratorImpl<T>
		extends
		BindingNodeConfiguratorImpl<DataBindingTypeConfigurator<T>, DataBindingType<T>, T>
		implements DataBindingTypeConfigurator<T> {
	public static class DataBindingTypeImpl<T> extends
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

				baseType = overrideMerge.configurator().baseType == null ? null
						: overrideMerge.configurator().baseType.effective();
			}

			@Override
			public Boolean isPrivate() {
				return isPrivate;
			}

			@Override
			public DataBindingType.Effective<? super T> baseType() {
				return baseType;
			}
		}

		private final Effective<T> effective;

		private final Boolean isPrivate;

		private final DataBindingType<? super T> baseType;

		public DataBindingTypeImpl(DataBindingTypeConfiguratorImpl<T> configurator) {
			super(configurator);

			isPrivate = configurator.isPrivate;

			baseType = configurator.baseType;

			effective = new Effective<>(overrideMerge(this, configurator));
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
		public Effective<T> effective() {
			return effective;
		}
	}

	private final DataLoader loader;

	private Boolean isPrivate;

	private DataBindingType<? super T> baseType;

	public DataBindingTypeConfiguratorImpl(DataLoader loader) {
		this.loader = loader;
	}

	@Override
	protected DataBindingType<T> tryCreate() {
		return new DataBindingTypeImpl<>(this);
	}

	@Override
	public DataBindingTypeConfigurator<T> isPrivate(boolean isPrivate) {
		assertConfigurable(this.isPrivate);
		this.isPrivate = isPrivate;

		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public final <U extends T> DataBindingTypeConfigurator<U> baseType(
			DataBindingType<? super U> baseType) {
		assertConfigurable(this.baseType);
		this.baseType = (DataBindingType<? super T>) baseType;

		return (DataBindingTypeConfigurator<U>) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V extends T> DataBindingTypeConfigurator<V> dataType(
			TypeLiteral<V> dataClass) {
		return (DataBindingTypeConfigurator<V>) super.dataType(dataClass);
	}

	@Override
	protected Namespace getNamespace() {
		return getName().getNamespace();
	}

	@Override
	protected boolean isDataContext() {
		return true;
	}

	@SuppressWarnings("serial")
	@Override
	protected TypeToken<DataBindingType<T>> getNodeClass() {
		return new TypeToken<DataBindingType<T>>() {
		};
	}

	@Override
	protected DataLoader getDataLoader() {
		return loader;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<DataBindingType<T>> getOverriddenNodes() {
		return baseType == null ? Collections.emptyList() : new ArrayList<>(
				Arrays.asList((DataBindingType<T>) baseType));
	}
}
