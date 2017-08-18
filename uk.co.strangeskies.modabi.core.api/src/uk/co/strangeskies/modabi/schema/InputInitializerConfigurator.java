package uk.co.strangeskies.modabi.schema;

import uk.co.strangeskies.modabi.schema.expression.ValueExpression;

public interface InputInitializerConfigurator extends IOConfigurator {
	ValueExpression<Object> parent();

	void expression(ValueExpression<?> expression);
}
