package uk.co.strangeskies.modabi.data.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import uk.co.strangeskies.modabi.data.DataBindingType;
import uk.co.strangeskies.modabi.data.DataBindingTypeConfigurator;
import uk.co.strangeskies.modabi.model.building.DataLoader;
import uk.co.strangeskies.modabi.model.building.configurators.impl.BindingNodeConfiguratorImpl;
import uk.co.strangeskies.modabi.model.building.impl.OverrideMerge;
import uk.co.strangeskies.modabi.model.nodes.DataNode;
import uk.co.strangeskies.modabi.model.nodes.DataNodeChildNode;

public class DataBindingTypeConfiguratorImpl<T>
		extends
		BindingNodeConfiguratorImpl<DataBindingTypeConfigurator<T>, DataBindingType<T>, T, DataNodeChildNode<?>, DataNode<?>>
		implements DataBindingTypeConfigurator<T> {
	public static class DataBindingTypeImpl<T> extends
			BindingNodeImpl<T, DataBindingType.Effective<T>> implements
			DataBindingType<T> {
		private static class Effective<T> extends
				BindingNodeImpl.Effective<T, DataBindingType.Effective<T>> implements
				DataBindingType.Effective<T> {
			private final DataBindingType<T> source;

			private final Boolean isAbstract;
			private final Boolean isPrivate;

			private final DataBindingType.Effective<? super T> baseType;

			public Effective(
					OverrideMerge<DataBindingType<T>, DataBindingTypeConfiguratorImpl<T>> overrideMerge) {
				super(overrideMerge);

				source = overrideMerge.node();

				isAbstract = overrideMerge.getValue(DataBindingType::isAbstract);
				isPrivate = overrideMerge.getValue(DataBindingType::isPrivate);

				baseType = overrideMerge.configurator().baseType == null ? null
						: overrideMerge.configurator().baseType.effective();
			}

			@Override
			public DataBindingType<T> source() {
				return source;
			}

			@Override
			public Boolean isAbstract() {
				return isAbstract;
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

		private final Boolean isAbstract;
		private final Boolean isPrivate;

		private final DataBindingType<? super T> baseType;

		public DataBindingTypeImpl(DataBindingTypeConfiguratorImpl<T> configurator) {
			super(configurator);

			isAbstract = configurator.isAbstract;
			isPrivate = configurator.isPrivate;

			baseType = configurator.baseType;

			effective = new Effective<>(overrideMerge(this, configurator));
		}

		@Override
		public Boolean isAbstract() {
			return isAbstract;
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

	private Boolean isAbstract;
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
	public DataBindingTypeConfigurator<T> isAbstract(boolean isAbstract) {
		requireConfigurable(this.isAbstract);
		this.isAbstract = isAbstract;

		return this;
	}

	@Override
	public DataBindingTypeConfigurator<T> isPrivate(boolean isPrivate) {
		requireConfigurable(this.isPrivate);
		this.isPrivate = isPrivate;

		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public final <U extends T> DataBindingTypeConfigurator<U> baseType(
			DataBindingType<? super U> baseType) {
		requireConfigurable(this.baseType);
		this.baseType = (DataBindingType<? super T>) baseType;

		return (DataBindingTypeConfigurator<U>) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V extends T> DataBindingTypeConfigurator<V> dataClass(
			Class<V> dataClass) {
		return (DataBindingTypeConfigurator<V>) super.dataClass(dataClass);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Class<DataBindingType<T>> getNodeClass() {
		return (Class<DataBindingType<T>>) (Object) DataBindingType.class;
	}

	@Override
	protected DataLoader getDataLoader() {
		return loader;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Set<DataBindingType<T>> getOverriddenNodes() {
		return baseType == null ? Collections.emptySet() : new HashSet<>(
				Arrays.asList((DataBindingType<T>) baseType));
	}

	@Override
	protected boolean isAbstract() {
		return (isAbstract != null && isAbstract)
				|| getOverriddenNodes().stream().anyMatch(
						m -> m.effective().isAbstract());
	}
}
