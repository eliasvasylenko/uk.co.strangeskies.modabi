package uk.co.strangeskies.modabi.schema;

import uk.co.strangeskies.reflection.codegen.ValueExpression;

public interface OutputInitializerConfigurator<T> extends IOConfigurator {
	ValueExpression<Object> parent();

	ValueExpression<T> outputObject();

	void expression(ValueExpression<?> expression);
}
