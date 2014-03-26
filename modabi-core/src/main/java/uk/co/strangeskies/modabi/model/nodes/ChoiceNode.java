package uk.co.strangeskies.modabi.model.nodes;

public interface ChoiceNode extends ChildNode {
	public Boolean isMandatory();

	@Override
	public default Class<?> getPreInputClass() {
		Class<?> inputClass = null;
		for (ChildNode child : getChildren()) {
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
	public default Class<?> getPostInputClass() {
		Class<?> outputClass = null;
		for (ChildNode child : getChildren()) {
			Class<?> nextOutputClass = child.getPostInputClass();
			if (outputClass != null)
				if (nextOutputClass.isAssignableFrom(outputClass))
					outputClass = nextOutputClass;
				else if (!outputClass.isAssignableFrom(nextOutputClass))
					return Object.class;
		}
		return outputClass;
	}
}
