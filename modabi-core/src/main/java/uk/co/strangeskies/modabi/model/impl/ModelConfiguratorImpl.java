package uk.co.strangeskies.modabi.model.impl;

import java.util.List;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.model.EffectiveModel;
import uk.co.strangeskies.modabi.model.Model;
import uk.co.strangeskies.modabi.model.building.ModelConfigurator;

class ModelConfiguratorImpl<T> extends
		AbstractModelConfiguratorImpl<ModelConfigurator<T>, Model<T>, T> implements
		ModelConfigurator<T> {
	public static class EffectiveModelImpl<T> extends AbstractModelImpl<T>
			implements EffectiveModel<T> {
		public EffectiveModelImpl(ModelImpl<? super T> node,
				List<? extends EffectiveModel<? super T>> overriddenNodes) {
			super(node, overriddenNodes);
		}
	}

	protected static class ModelImpl<T> extends AbstractModelImpl<T> implements
			Model<T> {
		private final EffectiveModel<T> effectiveModel;

		public ModelImpl(ModelConfiguratorImpl<T> configurator) {
			super(configurator);

			effectiveModel = new EffectiveModelImpl<T>(this, getBaseModel().stream()
					.map(m -> m.effectiveModel()).collect(Collectors.toList()));
		}

		@Override
		public EffectiveModel<T> effectiveModel() {
			return effectiveModel;
		}

		@Override
		protected void validateAsEffectiveModel(boolean isAbstract) {
			super.validateAsEffectiveModel(isAbstract);
		}
	}

	public ModelConfiguratorImpl(BranchingNodeConfiguratorImpl<?, ?> parent) {
		super(parent);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V extends T> ModelConfiguratorImpl<V> dataClass(Class<V> dataClass) {
		return (ModelConfiguratorImpl<V>) super.dataClass(dataClass);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V extends T> ModelConfiguratorImpl<V> baseModel(
			Model<? super V>... base) {
		return (ModelConfiguratorImpl<V>) super.baseModel(base);
	}

	@Override
	public Model<T> tryCreate() {
		return new ModelImpl<>(this);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<Model<T>> getNodeClass() {
		return (Class<Model<T>>) (Object) Model.class;
	}
}
