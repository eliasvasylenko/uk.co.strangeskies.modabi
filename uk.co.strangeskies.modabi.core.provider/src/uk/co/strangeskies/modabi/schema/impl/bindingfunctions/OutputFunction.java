package uk.co.strangeskies.modabi.schema.impl.bindingfunctions;

import static uk.co.strangeskies.reflection.token.TypeToken.forType;

import java.util.function.Consumer;

import uk.co.strangeskies.modabi.binding.BindingContext;
import uk.co.strangeskies.modabi.expression.Expression;
import uk.co.strangeskies.modabi.expression.functional.FunctionImplementation;
import uk.co.strangeskies.modabi.expression.functional.FunctionalExpressionCompiler;
import uk.co.strangeskies.modabi.schema.BindingFunction;
import uk.co.strangeskies.reflection.InferenceVariable;
import uk.co.strangeskies.reflection.token.TypeToken;

public class OutputFunction implements BindingFunction {
  private final Expression expression;
  private final FunctionImplementation<Consumer<BindingContext>> bindingFunction;
  private final TypeToken<?> nextTargetType = forType(new InferenceVariable("TARGET"));

  public OutputFunction(Expression expression, FunctionalExpressionCompiler compiler) {
    this.expression = visitor -> expression
        .evaluate(new BindingFunctionPreprocessor(visitor, null));
    this.bindingFunction = compiler
        .compile(expression, new TypeToken<Consumer<BindingContext>>() {});
  }

  @Override
  public Expression getExpression() {
    return expression;
  }

  @Override
  public void apply(BindingContext context) {
    bindingFunction.getInstance().accept(context);
  }
}
