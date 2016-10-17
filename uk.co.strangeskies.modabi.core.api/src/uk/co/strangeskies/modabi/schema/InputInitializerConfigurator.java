package uk.co.strangeskies.modabi.schema;

import uk.co.strangeskies.reflection.codegen.ValueExpression;

public interface InputInitializerConfigurator extends IOConfigurator {
	ValueExpression<Object> parent();

	void expression(ValueExpression<?> expression);
}
