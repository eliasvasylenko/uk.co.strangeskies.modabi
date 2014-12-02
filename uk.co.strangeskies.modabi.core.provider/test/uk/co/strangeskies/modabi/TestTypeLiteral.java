package uk.co.strangeskies.modabi;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;

import com.google.common.reflect.Invokable;
import com.google.common.reflect.TypeToken;

@SuppressWarnings({ "serial", "rawtypes" })
public class TestTypeLiteral {
	public class A<T> {
		public class B {}
	}

	public static void main(String... args) throws NoSuchMethodException,
			SecurityException {
		TypeToken<Consumer<? super Comparable<String>>> literal = new TypeToken<Consumer<? super Comparable<String>>>() {};

		Invokable<Consumer<? super Comparable<String>>, ?> accept = literal
				.method(literal.getRawType().getMethod("accept", Object.class));

		@SuppressWarnings("unchecked")
		TypeToken<? super Comparable<String>> boundedLiteral = (TypeToken<? super Comparable<String>>) accept
				.getParameters().get(0).getType();

		System.out.println(boundedLiteral);
		System.out.println(boundedLiteral.isAssignableFrom(TypeToken
				.of(String.class)));

		System.out.println(" "
				+ new TypeToken<Number>() {}
						.isAssignableFrom(new TypeToken<Integer>() {}));

		TypeToken<?> ab = new TypeToken<A<String>.B>() {};
		System.out.println(((ParameterizedType) ab.getType()).getOwnerType());
		System.out.println(((ParameterizedType) ab.getType()));

		TypeToken<?> ab2 = new TypeToken<A.B>() {};

		System.out.println(ab2.resolveType(ab.getRawType().getEnclosingClass()
				.getTypeParameters()[0]));

		System.out.println(tester(new TypeToken<Collection>() {},
				new TypeToken<Collection<String>>() {}));

		System.out.println(tester(new TypeToken<Set>() {},
				new TypeToken<Collection<String>>() {}));

		System.out.println(tester(new TypeToken<Set<String>>() {},
				new TypeToken<Collection<String>>() {}));

		System.out.println(tester(new TypeToken<Set>() {},
				new TypeToken<Collection>() {}));

		System.out.println(tester(new TypeToken<Set<?>>() {},
				new TypeToken<Collection>() {}));

		lolwat();
	}

	static <V> void lolwat() {
		System.out.println(tester(new TypeToken<Set<V>>() {},
				new TypeToken<Collection<String>>() {}));
	}

	@SuppressWarnings("unchecked")
	public static String tester(TypeToken<?> from, TypeToken<?> to) {
		if (to.getType() instanceof ParameterizedType)
			if (to.getRawType().isAssignableFrom(from.getRawType())) {
				Type fsta = ((ParameterizedType) from.getSupertype(
						(Class<Object>) to.getRawType()).getType())
						.getActualTypeArguments()[0];
				if (fsta instanceof TypeVariable
						&& ((TypeVariable<?>) fsta).getGenericDeclaration() instanceof Class)
					return "all conditions!";
				else
					return "two conditions    "
							+ from.getSupertype((Class<Object>) to.getRawType()).getType();
			} else
				return "one condition";

		return "no conditions";
	}
}
