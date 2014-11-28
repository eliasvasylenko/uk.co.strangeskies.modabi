package uk.co.strangeskies.modabi;

import java.lang.reflect.TypeVariable;

import uk.co.strangeskies.modabi.io.DataItem;
import uk.co.strangeskies.modabi.io.DataTarget;

import com.google.common.reflect.Invokable;
import com.google.common.reflect.TypeResolver;
import com.google.common.reflect.TypeToken;

public class TestGenericMethodReflection {
	@SuppressWarnings("serial")
	public static void main(String... args) throws NoSuchMethodException,
			SecurityException {
		TypeToken<DataTarget> literal = new TypeToken<DataTarget>() {
		};

		Invokable<DataTarget, ?> method = literal.method(literal.getRawType()
				.getMethod("put", DataItem.class));

		TypeVariable<?>[] params = method.getTypeParameters();

		System.out.println(new TypeResolver().where(params[0], Integer.class)
				.resolveType(method.getParameters().get(0).getType().getType()));
	}
}
