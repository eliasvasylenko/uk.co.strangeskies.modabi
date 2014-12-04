package uk.co.strangeskies.modabi.types;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Executable;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

public class InferenceVariable implements TypeVariable<Executable> {
	private final TypeVariable<? extends Executable> typeVariable;

	public InferenceVariable(TypeVariable<? extends Executable> typeVariable) {
		this.typeVariable = typeVariable;
	}

	@Override
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		return typeVariable.getAnnotation(annotationClass);
	}

	@Override
	public Annotation[] getAnnotations() {
		return typeVariable.getAnnotations();
	}

	@Override
	public Annotation[] getDeclaredAnnotations() {
		return typeVariable.getDeclaredAnnotations();
	}

	@Override
	public Type[] getBounds() {
		return typeVariable.getBounds();
	}

	@Override
	public Executable getGenericDeclaration() {
		return typeVariable.getGenericDeclaration();
	}

	@Override
	public String getName() {
		return typeVariable.getName();
	}

	@Override
	public AnnotatedType[] getAnnotatedBounds() {
		return typeVariable.getAnnotatedBounds();
	}

	@Override
	public boolean equals(Object obj) {
		return typeVariable.equals(obj);
	}

	@Override
	public int hashCode() {
		return typeVariable.hashCode();
	}
}
