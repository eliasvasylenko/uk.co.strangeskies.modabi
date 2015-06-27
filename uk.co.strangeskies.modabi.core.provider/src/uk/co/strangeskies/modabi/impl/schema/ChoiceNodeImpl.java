package uk.co.strangeskies.modabi.impl.schema;

import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.impl.schema.utilities.OverrideMerge;
import uk.co.strangeskies.modabi.schema.ChildNode;
import uk.co.strangeskies.modabi.schema.ChoiceNode;
import uk.co.strangeskies.reflection.TypeToken;

class ChoiceNodeImpl extends SchemaNodeImpl<ChoiceNode, ChoiceNode.Effective>
		implements ChoiceNode {
	private static class Effective extends
			SchemaNodeImpl.Effective<ChoiceNode, ChoiceNode.Effective> implements
			ChoiceNode.Effective {
		private final boolean mandatory;

		private final TypeToken<?> preInputClass;
		private final TypeToken<?> postInputClass;

		public Effective(
				OverrideMerge<ChoiceNode, ChoiceNodeConfiguratorImpl> overrideMerge) {
			super(overrideMerge);

			TypeToken<?> preInputClass = null;
			if (!isAbstract())
				for (ChildNode.Effective<?, ?> child : children()) {
					TypeToken<?> nextInputClass = child.getPreInputType();
					if (preInputClass != null)
						if (preInputClass.isAssignableFrom(nextInputClass))
							preInputClass = nextInputClass;
						else if (!nextInputClass.isAssignableFrom(preInputClass))
							throw new IllegalArgumentException();
				}
			this.preInputClass = preInputClass;

			TypeToken<?> postInputClass = overrideMerge.tryGetValue(
					ChildNode::getPostInputType, TypeToken::isAssignableTo);
			if (!isAbstract())
				if (postInputClass == null)
					for (ChildNode.Effective<?, ?> child : children()) {
						TypeToken<?> nextOutputClass = child.getPostInputType();
						if (postInputClass != null)
							if (nextOutputClass.isAssignableFrom(postInputClass))
								postInputClass = nextOutputClass;
							else if (!postInputClass.isAssignableFrom(nextOutputClass))
								postInputClass = TypeToken.over(Object.class);
					}
				else
					for (ChildNode.Effective<?, ?> child : children())
						if (!postInputClass.isAssignableFrom(child.getPostInputType()))
							throw new SchemaException();
			this.postInputClass = postInputClass;

			mandatory = overrideMerge.getValue(ChoiceNode::isMandatory, false);
		}

		@Override
		public TypeToken<?> getPreInputType() {
			return preInputClass;
		}

		@Override
		public TypeToken<?> getPostInputType() {
			return postInputClass;
		}

		@Override
		public Boolean isMandatory() {
			return mandatory;
		}
	}

	private final ChoiceNodeImpl.Effective effective;

	private final TypeToken<?> postInputClass;
	private final Boolean mandatory;

	public ChoiceNodeImpl(ChoiceNodeConfiguratorImpl configurator) {
		super(configurator);

		postInputClass = configurator.getPostInputClass();
		mandatory = configurator.getMandatory();

		effective = new Effective(ChoiceNodeConfiguratorImpl.overrideMerge(this,
				configurator));
	}

	@Override
	public final Boolean isMandatory() {
		return mandatory;
	}

	@Override
	public ChoiceNodeImpl.Effective effective() {
		return effective;
	}

	@Override
	public TypeToken<?> getPostInputType() {
		return postInputClass;
	}
}
