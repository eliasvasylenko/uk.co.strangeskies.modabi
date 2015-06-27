package uk.co.strangeskies.modabi.impl.schema;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.impl.schema.utilities.OverrideMerge;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.SchemaNode;

class ModelImpl<T> extends BindingNodeImpl<T, Model<T>, Model.Effective<T>>
		implements Model<T> {
	private static class Effective<T> extends
			BindingNodeImpl.Effective<T, Model<T>, Model.Effective<T>> implements
			Model.Effective<T> {
		private final List<Model.Effective<? super T>> baseModel;

		protected Effective(
				OverrideMerge<Model<T>, ModelConfiguratorImpl<T>> overrideMerge) {
			super(overrideMerge);

			List<Model.Effective<? super T>> baseModel = new ArrayList<>();
			overrideMerge.configurator().getOverriddenNodes()
					.forEach(n -> baseModel.addAll(n.effective().baseModel()));
			baseModel.addAll(overrideMerge.node().baseModel().stream()
					.map(SchemaNode::effective).collect(Collectors.toSet()));
			this.baseModel = Collections.unmodifiableList(baseModel);
		}

		@Override
		public final List<Model.Effective<? super T>> baseModel() {
			return baseModel;
		}

		@Override
		protected QualifiedName defaultName(
				OverrideMerge<Model<T>, ? extends SchemaNodeConfiguratorImpl<?, Model<T>>> overrideMerge) {
			List<Model.Effective<? super T>> baseModel = new ArrayList<>();
			overrideMerge.configurator().getOverriddenNodes()
					.forEach(n -> baseModel.addAll(n.effective().baseModel()));
			baseModel.addAll(overrideMerge.node().baseModel().stream()
					.map(SchemaNode::effective).collect(Collectors.toSet()));

			return (baseModel == null || baseModel.size() != 1) ? null : baseModel
					.get(0).getName();
		}
	}

	private final ModelImpl.Effective<T> effective;

	private final List<Model<? super T>> baseModel;

	public ModelImpl(ModelConfiguratorImpl<T> configurator) {
		super(configurator);

		baseModel = configurator.getBaseModel() == null ? Collections.emptyList()
				: Collections.unmodifiableList(new ArrayList<>(configurator
						.getBaseModel()));

		effective = new ModelImpl.Effective<>(ModelConfiguratorImpl.overrideMerge(
				this, configurator));
	}

	@Override
	public final List<Model<? super T>> baseModel() {
		return baseModel;
	}

	@Override
	public ModelImpl.Effective<T> effective() {
		return effective;
	}
}
