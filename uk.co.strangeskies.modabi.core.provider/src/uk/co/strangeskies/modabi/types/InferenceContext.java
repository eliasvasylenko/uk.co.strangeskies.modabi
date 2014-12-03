package uk.co.strangeskies.modabi.types;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.reflect.TypeResolver;
import com.google.common.reflect.TypeToken;

class InferenceContext {
	private final Map<TypeVariable<?>, InferenceVariable> inferenceVariables;
	private final Map<InferenceVariable, Type> instantiations;

	public InferenceContext() {
		inferenceVariables = new HashMap<>();
		instantiations = new HashMap<>();
	}

	public boolean isInferenceVariable(Type type) {
		return inferenceVariables.keySet().contains(type);
	}

	public InferenceVariable getInferenceVariable(Type type) {
		return inferenceVariables.get((TypeVariable<?>) type);
	}

	public Collection<InferenceVariable> getInferenceVariables() {
		return inferenceVariables.values();
	}

	public boolean isInstantiated(InferenceVariable variable) {
		return instantiations.keySet().contains(variable);
	}

	public Type getInstantiation(InferenceVariable variable) {
		return instantiations.get(variable);
	}

	public boolean isProper(Type type) {
		if (type == null)
			return false;

		class Unique {}

		TypeResolver resolver = new TypeResolver();

		for (InferenceVariable variable : getInferenceVariables())
			resolver = resolver.where(variable.getTypeVariable(), Unique.class);

		return TypeToken.of(resolver.resolveType(type)).equals(TypeToken.of(type));
	}
}
