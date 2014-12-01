package uk.co.strangeskies.modabi.types;

import java.lang.reflect.TypeVariable;

public class InferenceVariable {
	private final TypeVariable<?> typeVariable;

	public InferenceVariable(TypeVariable<?> typeVariable) {
		this.typeVariable = typeVariable;
	}

	public TypeVariable<?> getTypeVariable() {
		return typeVariable;
	}
}
