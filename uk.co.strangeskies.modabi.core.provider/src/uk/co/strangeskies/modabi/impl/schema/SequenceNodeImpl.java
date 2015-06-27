package uk.co.strangeskies.modabi.impl.schema;

import uk.co.strangeskies.modabi.impl.schema.utilities.OverrideMerge;
import uk.co.strangeskies.modabi.schema.ChildNode;
import uk.co.strangeskies.modabi.schema.SequenceNode;
import uk.co.strangeskies.reflection.TypeToken;

class SequenceNodeImpl extends
		SchemaNodeImpl<SequenceNode, SequenceNode.Effective> implements
		SequenceNode {
	private class Effective extends
			SchemaNodeImpl.Effective<SequenceNode, SequenceNode.Effective> implements
			SequenceNode.Effective {
		private final TypeToken<?> preInputClass;
		private final TypeToken<?> postInputClass;

		public Effective(
				OverrideMerge<SequenceNode, SequenceNodeConfiguratorImpl> overrideMerge) {
			super(overrideMerge);

			preInputClass = isAbstract() ? null : children().get(0).getPreInputType();

			TypeToken<?> postInputClass = overrideMerge.tryGetValue(
					ChildNode::getPostInputType, TypeToken::isAssignableTo);
			if (postInputClass == null && !isAbstract()) {
				for (ChildNode.Effective<?, ?> child : children()) {
					if (postInputClass != null
							&& !child.getPreInputType().isAssignableFrom(postInputClass)) {
						throw new IllegalArgumentException();
					}
					postInputClass = child.getPostInputType();
				}
			}
			this.postInputClass = postInputClass;
		}

		@Override
		public TypeToken<?> getPreInputType() {
			return preInputClass;
		}

		@Override
		public TypeToken<?> getPostInputType() {
			return postInputClass;
		}
	}

	private final Effective effective;
	private final TypeToken<?> postInputClass;

	public SequenceNodeImpl(SequenceNodeConfiguratorImpl configurator) {
		super(configurator);

		postInputClass = configurator.getPostInputClass();

		effective = new Effective(SequenceNodeConfiguratorImpl.overrideMerge(this,
				configurator));
	}

	@Override
	public Effective effective() {
		return effective;
	}

	@Override
	public TypeToken<?> getPostInputType() {
		return postInputClass;
	}
}
