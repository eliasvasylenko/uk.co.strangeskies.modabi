package uk.co.strangeskies.modabi.schema.model.building.configurators.impl;

import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.model.building.configurators.ChoiceNodeConfigurator;
import uk.co.strangeskies.modabi.schema.model.building.impl.ChildNodeImpl;
import uk.co.strangeskies.modabi.schema.model.building.impl.ChildrenConfigurator;
import uk.co.strangeskies.modabi.schema.model.building.impl.OverrideMerge;
import uk.co.strangeskies.modabi.schema.model.building.impl.SchemaNodeConfigurationContext;
import uk.co.strangeskies.modabi.schema.model.nodes.BindingChildNode;
import uk.co.strangeskies.modabi.schema.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.schema.model.nodes.ChoiceNode;

public class ChoiceNodeConfiguratorImpl<C extends ChildNode<?, ?>, B extends BindingChildNode<?, ?, ?>>
		extends
		ChildNodeConfiguratorImpl<ChoiceNodeConfigurator<C, B>, ChoiceNode, C, B>
		implements ChoiceNodeConfigurator<C, B> {
	protected static class ChoiceNodeImpl extends
			SchemaNodeImpl<ChoiceNode, ChoiceNode.Effective> implements
			ChildNodeImpl<ChoiceNode, ChoiceNode.Effective>, ChoiceNode {
		private static class Effective extends
				SchemaNodeImpl.Effective<ChoiceNode, ChoiceNode.Effective> implements
				ChoiceNode.Effective {
			private final boolean mandatory;

			private final Class<?> preInputClass;
			private final Class<?> postInputClass;

			public Effective(
					OverrideMerge<ChoiceNode, ChoiceNodeConfiguratorImpl<?, ?>> overrideMerge) {
				super(overrideMerge);

				Class<?> preInputClass = null;
				if (!isAbstract())
					for (ChildNode.Effective<?, ?> child : children()) {
						Class<?> nextInputClass = child.getPreInputClass();
						if (preInputClass != null)
							if (preInputClass.isAssignableFrom(nextInputClass))
								preInputClass = nextInputClass;
							else if (!nextInputClass.isAssignableFrom(preInputClass))
								throw new IllegalArgumentException();
					}
				this.preInputClass = preInputClass;

				Class<?> postInputClass = overrideMerge.tryGetValue(
						ChildNode::getPostInputClass, (n, o) -> o.isAssignableFrom(n));
				if (!isAbstract())
					if (postInputClass == null)
						for (ChildNode.Effective<?, ?> child : children()) {
							Class<?> nextOutputClass = child.getPostInputClass();
							if (postInputClass != null)
								if (nextOutputClass.isAssignableFrom(postInputClass))
									postInputClass = nextOutputClass;
								else if (!postInputClass.isAssignableFrom(nextOutputClass))
									postInputClass = Object.class;
						}
					else
						for (ChildNode.Effective<?, ?> child : children())
							if (!postInputClass.isAssignableFrom(child.getPostInputClass()))
								throw new SchemaException();
				this.postInputClass = postInputClass;

				mandatory = overrideMerge.getValue(ChoiceNode::isMandatory);
			}

			@Override
			public Class<?> getPreInputClass() {
				return preInputClass;
			}

			@Override
			public Class<?> getPostInputClass() {
				return postInputClass;
			}

			@Override
			public Boolean isMandatory() {
				return mandatory;
			}
		}

		private final Effective effective;

		private final Class<?> postInputClass;
		private final boolean mandatory;

		public ChoiceNodeImpl(ChoiceNodeConfiguratorImpl<?, ?> configurator) {
			super(configurator);

			postInputClass = configurator.getPostInputClass();
			mandatory = configurator.mandatory;

			effective = new Effective(overrideMerge(this, configurator));
		}

		@Override
		public final Boolean isMandatory() {
			return mandatory;
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

	private boolean mandatory;

	public ChoiceNodeConfiguratorImpl(
			SchemaNodeConfigurationContext<? super ChildNode<?, ?>> parent) {
		super(parent);
	}

	@Override
	public ChoiceNode tryCreate() {
		return new ChoiceNodeImpl(this);
	}

	@Override
	public ChildrenConfigurator<C, B> createChildrenConfigurator() {
		return null; // TODO create hiding children configurator! options can be
									// reduced, not increased, but overriding nodes.
	}

	@Override
	public ChoiceNodeConfigurator<C, B> mandatory(boolean mandatory) {
		this.mandatory = mandatory;

		return this;
	}

	@Override
	protected Class<ChoiceNode> getNodeClass() {
		return ChoiceNode.class;
	}
}
