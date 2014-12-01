package uk.co.strangeskies.modabi.types;

import java.lang.reflect.Type;
import java.util.Map;

public interface BoundVisitor<T> {
	public T acceptEqual(InferenceVariable a, InferenceVariable b);

	public T acceptEqual(Type a, InferenceVariable b);

	public T acceptSubtype(InferenceVariable a, InferenceVariable b);

	public T acceptSubtype(Type a, InferenceVariable b);

	public T acceptSubtype(InferenceVariable a, Type b);

	public T acceptFalsehood();

	public T acceptCaptureConversion(Map<Type, InferenceVariable> c);
}
