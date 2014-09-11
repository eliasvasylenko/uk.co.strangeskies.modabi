package uk.co.strangeskies.modabi.schema.model.building.configurators.impl;

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
			private final Class<?> preInputClass;
			private final Class<?> postInputClass;

			public Effective(
					OverrideMerge<SequenceNode, SequenceNodeConfiguratorImpl<?, ?>> overrideMerge) {
				super(overrideMerge);

				preInputClass = isAbstract() ? null : children().get(0)
						.getPreInputClass();

				Class<?> postInputClass = overrideMerge.tryGetValue(
						ChildNode::getPostInputClass, (n, o) -> o.isAssignableFrom(n));
				if (postInputClass == null && !isAbstract()) {
					for (ChildNode.Effective<?, ?> child : children()) {
						if (postInputClass != null
								&& !child.getPreInputClass().isAssignableFrom(postInputClass)) {
							throw new IllegalArgumentException();
						}
						postInputClass = child.getPostInputClass();
					}
				}
				this.postInputClass = postInputClass;
			}

			@Override
			public Class<?> getPreInputClass() {
				return preInputClass;
			}

			@Override
			public Class<?> getPostInputClass() {
				return postInputClass;
			}
		}

		private final Effective effective;

		private final Class<?> postInputClass;

		public SequenceNodeImpl(SequenceNodeConfiguratorImpl<?, ?> configurator) {
			super(configurator);

			postInputClass = configurator.getPostInputClass();

			effective = new Effective(overrideMerge(this, configurator));
		}

		@Override
		public Effective effective() {
			return effective;
		}

		@Override
		public Class<?> getPostInputClass() {
			return postInputClass;
		}
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
		Class<?> inputTarget = getContext().getInputTargetClass(getName());

		return new SequentialChildrenConfigurator<>(getNamespace(),
				getOverriddenNodes(), inputTarget, null, null, getContext()
						.isAbstract());
	}
}
