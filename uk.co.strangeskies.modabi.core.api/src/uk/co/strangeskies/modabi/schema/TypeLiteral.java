package uk.co.strangeskies.modabi.schema;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

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
