package uk.co.strangeskies.modabi.model.impl;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.model.EffectiveModel;
import uk.co.strangeskies.modabi.model.Model;
import uk.co.strangeskies.modabi.model.building.ModelConfigurator;
import uk.co.strangeskies.modabi.model.nodes.SchemaNode;

public class ModelConfiguratorImpl<T> extends
		AbstractModelConfiguratorImpl<ModelConfigurator<T>, Model<T>, T> implements
		ModelConfigurator<T> {
	protected static class EffectiveModelImpl<T> extends AbstractModelImpl<T>
			implements EffectiveModel<T> {
		public EffectiveModelImpl(ModelImpl<? super T> node,
				Collection<? extends EffectiveModel<? super T>> overriddenNodes,
				List<SchemaNode> effectiveChildren) {
			super(node, overriddenNodes, effectiveChildren);
		}
	}

	protected static class ModelImpl<T> extends AbstractModelImpl<T> implements
			Model<T> {
		private final EffectiveModel<T> effectiveModel;

		public ModelImpl(ModelConfiguratorImpl<T> configurator) {
			super(configurator);

			effectiveModel = new EffectiveModelImpl<T>(this, getBaseModel().stream()
					.map(m -> m.effectiveModel()).collect(Collectors.toList()),
					configurator.getEffectiveChildren());
		}

		@Override
		public EffectiveModel<T> effectiveModel() {
			return effectiveModel;
		}
	}

	public ModelConfiguratorImpl() {
		super(null);
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

	@Override
	protected Model<T> getEffective(Model<T> node) {
		return null;
	}
}
