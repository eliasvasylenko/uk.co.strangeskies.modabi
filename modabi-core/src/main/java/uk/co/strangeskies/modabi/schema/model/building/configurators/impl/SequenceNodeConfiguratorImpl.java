package uk.co.strangeskies.modabi.schema.model.building.configurators.impl;

import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.model.building.ChildBuilder;
import uk.co.strangeskies.modabi.schema.model.building.configurators.SequenceNodeConfigurator;
import uk.co.strangeskies.modabi.schema.model.building.impl.ChildNodeImpl;
import uk.co.strangeskies.modabi.schema.model.building.impl.ChildrenConfigurator;
import uk.co.strangeskies.modabi.schema.model.building.impl.OverrideMerge;
import uk.co.strangeskies.modabi.schema.model.building.impl.SchemaNodeConfigurationContext;
import uk.co.strangeskies.modabi.schema.model.building.impl.SequentialChildrenConfigurator;
import uk.co.strangeskies.modabi.schema.model.nodes.BindingChildNode;
import uk.co.strangeskies.modabi.schema.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.schema.model.nodes.SequenceNode;

public class SequenceNodeConfiguratorImpl<C extends ChildNode<?, ?>, B extends BindingChildNode<?, ?, ?>>
		extends
		ChildNodeConfiguratorImpl<SequenceNodeConfigurator<C, B>, SequenceNode, C, B>
		implements SequenceNodeConfigurator<C, B> {
	protected static class SequenceNodeImpl extends
			SchemaNodeImpl<SequenceNode, SequenceNode.Effective> implements
			ChildNodeImpl<SequenceNode, SequenceNode.Effective>, SequenceNode {
		private class Effective extends
				SchemaNodeImpl.Effective<SequenceNode, SequenceNode.Effective>
				implements SequenceNode.Effective {
			public Effective(
					OverrideMerge<SequenceNode, SequenceNodeConfiguratorImpl<?, ?>> overrideMerge) {
				super(overrideMerge);
			}
		}

		private final Effective effective;

		public SequenceNodeImpl(SequenceNodeConfiguratorImpl<?, ?> configurator) {
			super(configurator);

			effective = new Effective(overrideMerge(this, configurator));
		}

		@Override
		public Effective effective() {
			return effective;
		}
	}

	@Override
	public SequenceNodeConfigurator<C, B> name(String name) {
		return name(new QualifiedName(name, getContext().getNamespace()));
	}

	public SequenceNodeConfiguratorImpl(
			SchemaNodeConfigurationContext<? super SequenceNode> parent) {
		super(parent);
	}

	@Override
	public SequenceNode tryCreate() {
		return new SequenceNodeImpl(this);
	}

	@Override
	protected Class<SequenceNode> getNodeClass() {
		return SequenceNode.class;
	}

	@Override
	public ChildrenConfigurator<C, B> createChildrenConfigurator() {
		Class<?> inputTarget = getContext().getInputTargetClass();

		return new SequentialChildrenConfigurator<>(getNamespace(),
				getOverriddenNodes(), inputTarget, null, null, getContext()
						.isAbstract());
	}

	@Override
	public ChildBuilder<C, B> addChild() {
		return super.addChild();
	}
}
