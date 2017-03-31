package uk.co.strangeskies.modabi.schema;

import static uk.co.strangeskies.reflection.codegen.Expressions.typeTokenExpression;

import uk.co.strangeskies.modabi.processing.ProcessingContext;
import uk.co.strangeskies.reflection.codegen.ValueExpression;
import uk.co.strangeskies.reflection.token.ExecutableToken;
import uk.co.strangeskies.reflection.token.TypeArgument;
import uk.co.strangeskies.reflection.token.TypeToken;
import uk.co.strangeskies.reflection.token.TypedObject;

public interface IOConfigurator {
	<U> ValueExpression<U> none();

	ExecutableToken<ProcessingContext, ?> PROVIDE_METHOD = TypeToken
			.forClass(ProcessingContext.class)
			.methods()
			.named("provide")
			.resolveOverload(new TypeToken<TypeToken<?>>() {});

	/*
	 * TODO Scoping here deals only with sibling binding points. Dealing with
	 * others requires bringing them into scope explicitly.
	 * 
	 * (Do we bring them into scope through some standard mechanism? Or just by
	 * way of ProcessingContext#getBindingObject etc...)
	 */
	<U> ValueExpression<U> provideFor(BindingPoint<U> type);

	default <U> ValueExpression<U> provide(TypeToken<U> type) {
		TypeToken<TypedObject<U>> typedObject = new TypeToken<TypedObject<U>>() {}
				.withTypeArguments(new TypeArgument<U>(type) {});

		return context().invokeMethod(PROVIDE_METHOD.withTargetType(typedObject), typeTokenExpression(type)).invokeMethod(
				typedObject.methods().named("getObject").resolveOverload().withTargetType(new TypeToken<U>() {}));
	}

	default <U> ValueExpression<U> provide(Class<U> type) {
		return provide(TypeToken.forClass(type));
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
