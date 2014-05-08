package uk.co.strangeskies.modabi.model.nodes;

import uk.co.strangeskies.modabi.schema.processing.SchemaProcessingContext;

public interface SequenceNode extends ChildNode, DataNodeChildNode {
	@Override
	public default Class<?> getPreInputClass() {
		return getChildren().get(0).getPreInputClass();
	}

	@Override
	public default Class<?> getPostInputClass() {
		Class<?> outputClass = null;
		for (ChildNode child : getChildren()) {
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
