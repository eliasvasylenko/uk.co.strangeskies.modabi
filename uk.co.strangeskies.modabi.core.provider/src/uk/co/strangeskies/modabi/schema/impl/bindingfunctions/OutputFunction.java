package uk.co.strangeskies.modabi.schema.impl.bindingfunctions;

import uk.co.strangeskies.modabi.expression.Expression;
import uk.co.strangeskies.modabi.expression.functional.FunctionCapture;
import uk.co.strangeskies.modabi.expression.functional.FunctionalExpressionCompiler;
import uk.co.strangeskies.modabi.schema.BindingContext;
import uk.co.strangeskies.modabi.schema.BindingFunction;
import uk.co.strangeskies.modabi.schema.impl.ChildBindingPointBuilderImpl;
import uk.co.strangeskies.modabi.schema.impl.ChildBindingPointImpl;
import uk.co.strangeskies.reflection.token.TypeToken;

public class OutputFunction implements BindingFunction {
  public interface OutputFunctionInterface {
    void bind();
  }

  public class OutputFunctionCapture {
    public Object source;

    public BindingContext context;
  }

  private final Expression expression;
  private final FunctionCapture<OutputFunctionCapture, OutputFunctionInterface> bindingFunction;

  public OutputFunction(
      ChildBindingPointImpl<?> bindingPoint,
      ChildBindingPointBuilderImpl<?> bindingPointBuilder,
      Expression expression,
      FunctionalExpressionCompiler compiler) {
    this.expression = visitor -> expression
        .evaluate(new BindingFunctionPreprocessor(visitor, bindingPoint, bindingPointBuilder));
    this.bindingFunction = compiler
        .compile(
            expression,
            new TypeToken<OutputFunctionInterface>() {},
            new TypeToken<OutputFunctionCapture>() {});
  }

  @Override
  public Expression getExpression() {
    return expression;
  }

  @Override
  public void apply(BindingContext context) {
    OutputFunctionCapture capture = new OutputFunctionCapture();
    capture.source = context.getBindingObject();
    capture.context = context;
    bindingFunction.capture(capture).getInstance().bind();
  }
}
