package uk.co.strangeskies.modabi.schema;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

/**
 * TODO (?) This is hopefully a placeholder class. I'd prefer to not make
 * dependencies on an external TypeToken or TypeReference from Apache commons or
 * Guice, but there is currently no generic & typesafe representation of Types
 * in standard Java reflection tools. Hopefully there will be one day...
 *
 * This isn't a terribly important class, though, in the sense that the Modabi
 * API would benefit from lots of extra functionality. Its only purpose is as a
 * type marker for type safe building and retrieval of models, and for loading
 * of xml by type. If we are registering schema declaratively and loading xml by
 * injection we don't even need to look at this class.
 *
 * @param <T>
 */
public class TypeLiteral<T> {
	private final Type type;
	private final Class<? super T> rawClass;

	protected TypeLiteral() {
		type = ((ParameterizedType) getClass().getGenericSuperclass())
				.getActualTypeArguments()[0];
		rawClass = getRawType(type);
	}

	public TypeLiteral(Class<T> exactClass) {
		type = rawClass = exactClass;
	}

	private TypeLiteral(Type type) {
		this.type = type;
		rawClass = getRawType(type);
	}

	public static TypeLiteral<?> of(Type type) {
		return new TypeLiteral<>(type);
	}

	@SuppressWarnings("unchecked")
	private final Class<? super T> getRawType(Type type) {
		if (type instanceof TypeVariable) {
			return getRawType(((TypeVariable<?>) type).getBounds()[0]);
		} else if (type instanceof WildcardType) {
			return getRawType(((WildcardType) type).getUpperBounds()[0]);
		} else if (type instanceof ParameterizedType) {
			return (Class<? super T>) ((ParameterizedType) type).getRawType();
		} else if (type instanceof Class) {
			return (Class<? super T>) type;
		} else if (type instanceof GenericArrayType) {
			return (Class<? super T>) Array.newInstance(
					(getRawType(((GenericArrayType) type).getGenericComponentType())), 0)
					.getClass();
		}
		throw new IllegalArgumentException("Type of type '" + type
				+ "' is unsupported.");
	}

	public final Type type() {
		return type;
	}

	public final Class<? super T> rawClass() {
		return rawClass;
	}
}
