package uk.co.strangeskies.modabi.model.nodes;

import uk.co.strangeskies.modabi.schema.processing.SchemaProcessingContext;

public interface ChoiceNode extends
		ChildNode<ChoiceNode, ChoiceNode.Effective>,
		DataNodeChildNode<ChoiceNode, ChoiceNode.Effective> {
	interface Effective extends ChoiceNode,
			ChildNode.Effective<ChoiceNode, Effective> {
		@Override
		default Class<?> getPreInputClass() {
			Class<?> inputClass = null;
			for (ChildNode.Effective<?, ?> child : children()) {
				Class<?> nextInputClass = child.getPreInputClass();
				if (inputClass != null)
					if (inputClass.isAssignableFrom(nextInputClass))
						inputClass = nextInputClass;
					else if (!nextInputClass.isAssignableFrom(inputClass))
						throw new IllegalArgumentException();
			}
			return inputClass;
		}

		@Override
		default Class<?> getPostInputClass() {
			Class<?> outputClass = null;
			for (ChildNode.Effective<?, ?> child : children()) {
				Class<?> nextOutputClass = child.getPostInputClass();
				if (outputClass != null)
					if (nextOutputClass.isAssignableFrom(outputClass))
						outputClass = nextOutputClass;
					else if (!outputClass.isAssignableFrom(nextOutputClass))
						return Object.class;
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
		return ChoiceNode.Effective.class;
	}

	@Override
	default Class<ChoiceNode> getNodeClass() {
		return ChoiceNode.class;
	}

	Boolean isMandatory();
}
