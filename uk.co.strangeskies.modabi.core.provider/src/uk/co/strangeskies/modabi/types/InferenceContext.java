package uk.co.strangeskies.modabi.types;

import java.lang.reflect.Type;
import java.util.Collection;

interface InferenceContext {
	public abstract boolean isInferenceVariable(Type type);

	public abstract InferenceVariable getInferenceVariable(Type type);

	public abstract Collection<InferenceVariable> getInferenceVariables();

	public abstract boolean isProper(Type type);

}