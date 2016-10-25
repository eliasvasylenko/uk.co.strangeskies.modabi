package uk.co.strangeskies.modabi.schema;

import uk.co.strangeskies.reflection.codegen.Expression;
import uk.co.strangeskies.reflection.codegen.ValueExpression;
import uk.co.strangeskies.reflection.codegen.VariableExpression;

public interface InputConfigurator<T> extends IOConfigurator {
	ValueExpression<? extends T> result();

	VariableExpression<?> target();

	void expression(Expression inputExpression);

	Expression getExpression();
}
