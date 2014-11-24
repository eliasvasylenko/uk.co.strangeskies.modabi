package uk.co.strangeskies.modabi;

import java.util.function.Supplier;

import org.apache.commons.lang3.reflect.TypeUtils;

import com.google.common.reflect.TypeToken;

public class TestTypeLiteral {
	public static void main(String... args) {
		TypeToken<?> literal = TypeToken.of(TypeUtils.parameterize(
				Supplier.class,
				TypeUtils
						.wildcardType()
						.withLowerBounds(
								TypeUtils.parameterize(Comparable.class, String.class))
						.withUpperBounds(Object.class).build()));

		// TypeToken<?> literal = new TypeToken<Supplier<? super Integer>>() {
		// };

		try {
			System.out.println(literal.method(literal.getRawType().getMethod("get"))
					.getReturnType().isAssignableFrom(TypeToken.of(String.class)));
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
	}
}
