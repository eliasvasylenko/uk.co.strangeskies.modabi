package uk.co.strangeskies.modabi.model.nodes;

import uk.co.strangeskies.modabi.schema.processing.SchemaProcessingContext;

public interface SequenceNode extends
		ChildNode<SequenceNode, SequenceNode.Effective>,
		DataNodeChildNode<SequenceNode, SequenceNode.Effective> {
	interface Effective extends SequenceNode,
			ChildNode.Effective<SequenceNode, Effective> {
		@Override
		public default Class<?> getPreInputClass() {
			return children().isEmpty() ? null : children().get(0).getPreInputClass();
		}

		@Override
		public default Class<?> getPostInputClass() {
			Class<?> outputClass = null;
			for (ChildNode.Effective<?, ?> child : children()) {
				if (outputClass != null
						&& !child.getPreInputClass().isAssignableFrom(outputClass))
					throw new IllegalArgumentException();
				outputClass = child.getPostInputClass();
			}
			return outputClass;
		}

		@Override
		default void process(SchemaProcessingContext context) {
			context.accept(this);
		}
	}

	@Override
	default Class<Effective> getEffectiveClass() {
		return Effective.class;
	}

	@Override
	default Class<SequenceNode> getNodeClass() {
		return SequenceNode.class;
	}
}
