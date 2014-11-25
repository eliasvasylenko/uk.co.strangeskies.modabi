package uk.co.strangeskies.modabi;

import java.util.function.Consumer;

import org.apache.commons.lang3.reflect.TypeUtils;

import com.google.common.reflect.TypeToken;

public class TestTypeLiteral {
	public static void main(String... args) {
		TypeToken<?> literal = TypeToken.of(TypeUtils
				.parameterize(
						Consumer.class,
						TypeUtils
								.wildcardType()
								.withLowerBounds(
										TypeUtils.parameterize(Comparable.class, String.class))
								.build()));

		// TypeToken<?> literal = new TypeToken<Supplier<? super
		// Comparable<Integer>>>() {};

		try {
			System.out.println(literal.method(literal.getRawType().getMethod(
					"accept", Object.class)));
			System.out.println(literal
					.method(literal.getRawType().getMethod("accept", Object.class))
					.getParameters().get(0).getType());
			System.out.println(literal
					.method(literal.getRawType().getMethod("accept", Object.class))
					.getParameters().get(0).getType()
					.isAssignableFrom(TypeToken.of(String.class)));
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
	}
}
