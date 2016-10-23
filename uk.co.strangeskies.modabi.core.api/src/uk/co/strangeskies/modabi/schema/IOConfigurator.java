package uk.co.strangeskies.modabi.schema;

import static uk.co.strangeskies.reflection.TypeToken.overType;

import uk.co.strangeskies.modabi.processing.ProcessingContext;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.reflection.TypedObject;
import uk.co.strangeskies.reflection.codegen.ValueExpression;

public interface IOConfigurator {
	/*
	 * TODO Scoping here deals only with sibling binding points. Dealing with
	 * others requires bringing them into scope explicitly.
	 * 
	 * (Do we bring them into scope through some standard mechanism? Or just by
	 * way of ProcessingContext#getBindingObject etc...)
	 */
	<U> ValueExpression<U> provideFor(BindingPoint<U> type);

	default <U> ValueExpression<U> provide(TypeToken<U> type) {
		TypeToken<TypedObject<U>> typedObject = new TypeToken<TypedObject<U>>() {};

		return context()
				.invokeMethod(
						overType(ProcessingContext.class).findInterfaceMethod(p -> p.provide(type)).withTargetType(typedObject),
						(ValueExpression<TypeToken<U>>) null /* TODO */)
				.invokeMethod(typedObject.findInterfaceMethod(TypedObject::getObject).withTargetType(new TypeToken<U>() {}));
	}

	default <U> ValueExpression<U> provide(Class<U> type) {
		return provide(overType(type));
	}

	/**
	 * Get a value expression evaluating to a provision as per
	 * {@link #provide(TypeToken)}. The type provided is as declared as the
	 * {@link BindingPoint#dataType() data type} of the nearest containing binding
	 * point.
	 * 
	 * @return
	 */
	ValueExpression<?> provide();

	ValueExpression<ProcessingContext> context();

	ValueExpression<?> bound(String string);
}
