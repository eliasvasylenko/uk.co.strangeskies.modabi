package uk.co.strangeskies.modabi;

import java.lang.reflect.ParameterizedType;
import java.util.function.Consumer;

import com.google.common.reflect.Invokable;
import com.google.common.reflect.TypeToken;

public class TestTypeLiteral {
	public class A<T> {
		public class B {}
	}

	@SuppressWarnings("serial")
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
	}
}
