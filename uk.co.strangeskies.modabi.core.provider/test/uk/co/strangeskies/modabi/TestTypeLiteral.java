package uk.co.strangeskies.modabi;

import uk.co.strangeskies.modabi.types.InferenceProcessor;

import com.google.common.reflect.Invokable;

public class TestTypeLiteral {
	public class A<T> {
		public class B {}
	}

	public class B {
		public <T extends Number> void method(T a, T b) {}
	}

	public static void main(String... args) throws NoSuchMethodException,
			SecurityException {

		System.out.println(new InferenceProcessor(Invokable.from(B.class.getMethod("method",
				Number.class, Number.class)), null, String.class, String.class)
				.verifyLooseParameterApplicability());
	}
}
