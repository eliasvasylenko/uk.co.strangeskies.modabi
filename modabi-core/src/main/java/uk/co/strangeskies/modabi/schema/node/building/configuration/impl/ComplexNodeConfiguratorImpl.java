package uk.co.strangeskies.modabi.schema.node.building.configuration.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.node.ComplexNode;
import uk.co.strangeskies.modabi.schema.node.SchemaNode;
import uk.co.strangeskies.modabi.schema.node.building.configuration.ComplexNodeConfigurator;
import uk.co.strangeskies.modabi.schema.node.building.configuration.impl.utilities.OverrideMerge;
import uk.co.strangeskies.modabi.schema.node.building.configuration.impl.utilities.SchemaNodeConfigurationContext;
import uk.co.strangeskies.modabi.schema.node.model.Model;
import uk.co.strangeskies.modabi.schema.node.wrapping.impl.ComplexNodeWrapper;

public class ComplexNodeConfiguratorImpl<T>
		extends
		BindingChildNodeConfiguratorImpl<ComplexNodeConfigurator<T>, ComplexNode<T>, T>
		implements ComplexNodeConfigurator<T> {
	protected static class ComplexNodeImpl<T> extends
			BindingChildNodeImpl<T, ComplexNode<T>, ComplexNode.Effective<T>>
			implements ComplexNode<T> {
		private static class Effective<T>
				extends
				BindingChildNodeImpl.Effective<T, ComplexNode<T>, ComplexNode.Effective<T>>
				implements ComplexNode.Effective<T> {
			private final List<Model.Effective<? super T>> baseModel;

			private final boolean inline;

			protected Effective(
					OverrideMerge<ComplexNode<T>, ComplexNodeConfiguratorImpl<T>> overrideMerge) {
				super(overrideMerge);

				List<Model.Effective<? super T>> baseModel = new ArrayList<>();
				overrideMerge.configurator().getOverriddenNodes()
						.forEach(n -> baseModel.addAll(n.effective().baseModel()));
				baseModel.addAll(overrideMerge.node().baseModel().stream()
						.map(SchemaNode::effective).collect(Collectors.toSet()));
				this.baseModel = Collections.unmodifiableList(baseModel);

				this.inline = overrideMerge.getValue(ComplexNode::isInline, false);

				if (inline && isExtensible())
					throw new SchemaException("Complex node '" + getName()
							+ "' cannot be both inline and extensible.");
			}

			@Override
			public List<Model.Effective<? super T>> baseModel() {
				return baseModel;
			}

			@Override
			public Boolean isInline() {
				return inline;
			}
		}

		private final Effective<T> effective;

		private final List<Model<? super T>> baseModel;

		private final Boolean inline;

		public ComplexNodeImpl(ComplexNodeConfiguratorImpl<T> configurator) {
			super(configurator);

			baseModel = configurator.baseModel == null ? Collections.emptyList()
					: Collections
							.unmodifiableList(new ArrayList<>(configurator.baseModel));

			inline = configurator.inline;

			effective = new Effective<>(overrideMerge(this, configurator));
		}

		@Override
		public Effective<T> effective() {
			return effective;
		}

		@Override
		public final List<Model<? super T>> baseModel() {
			return baseModel;
		}

		@Override
		public Boolean isInline() {
			return inline;
		}
	}

	private List<Model<? super T>> baseModel;

	private Boolean inline;

	public ComplexNodeConfiguratorImpl(
			SchemaNodeConfigurationContext<? super ComplexNode<T>> parent) {
		super(parent);
	}

	@Override
	public ComplexNodeConfigurator<T> name(String name) {
		return name(new QualifiedName(name, getContext().namespace()));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V extends T> ComplexNodeConfigurator<V> baseModel(
			List<? extends Model<? super V>> base) {
		requireConfigurable(this.baseModel);
		baseModel = new ArrayList<>((List<? extends Model<? super T>>) base);

		return (ComplexNodeConfigurator<V>) this;
	}

	@Override
	public List<ComplexNode<T>> getOverriddenNodes() {
		List<ComplexNode<T>> overriddenNodes = new ArrayList<>();

		if (baseModel != null)
			for (Model<? super T> base : baseModel)
				overriddenNodes.add(new ComplexNodeWrapper<>(base.effective()));

		overriddenNodes.addAll(super.getOverriddenNodes());

		return overriddenNodes;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V extends T> ComplexNodeConfigurator<V> dataClass(Class<V> dataClass) {
		return (ComplexNodeConfigurator<V>) super.dataClass(dataClass);
	}

	@Override
	protected boolean isDataContext() {
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Class<ComplexNode<T>> getNodeClass() {
		return (Class<ComplexNode<T>>) (Object) ComplexNode.class;
	}

	@Override
	protected ComplexNode<T> tryCreate() {
		return new ComplexNodeImpl<>(this);
	}

	@Override
	public ComplexNodeConfigurator<T> inline(boolean inline) {
		requireConfigurable(this.inline);
		this.inline = inline;

		return this;
	}
}
