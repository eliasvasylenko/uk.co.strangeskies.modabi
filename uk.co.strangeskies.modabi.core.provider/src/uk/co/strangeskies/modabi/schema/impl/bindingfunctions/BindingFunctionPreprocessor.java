package uk.co.strangeskies.modabi.schema.impl.bindingfunctions;

import static java.util.Arrays.asList;
import static uk.co.strangeskies.modabi.expression.Expressions.literal;
import static uk.co.strangeskies.modabi.schema.BindingExpressions.BOUND_PREFIX;
import static uk.co.strangeskies.modabi.schema.BindingExpressions.OBJECT_VALUE;
import static uk.co.strangeskies.modabi.schema.BindingExpressions.PARENT_VALUE;
import static uk.co.strangeskies.modabi.schema.ModabiSchemaException.MESSAGES;

import uk.co.strangeskies.modabi.expression.Expression;
import uk.co.strangeskies.modabi.expression.ExpressionVisitor;
import uk.co.strangeskies.modabi.expression.Preprocessor;
import uk.co.strangeskies.modabi.schema.Child;
import uk.co.strangeskies.modabi.schema.ModabiSchemaException;

public class BindingFunctionPreprocessor implements Preprocessor {
  private final ExpressionVisitor expressionVisitor;
  private final BindingFunctionContext context;

  public BindingFunctionPreprocessor(
      ExpressionVisitor expressionVisitor,
      BindingFunctionContext context) {
    this.expressionVisitor = expressionVisitor;
    this.context = context;
  }

  @Override
  public ExpressionVisitor visitor() {
    return expressionVisitor;
  }

  private Expression rawBindingExpression(String bindingPoint) {
    return v -> v
        .visitInvocation(
            c -> c.visitNamed("context"),
            "getBoundObject",
            asList(literal(bindingPoint)));
  }

  private void visitBoundExpression(Child<?> bindingPoint) {
    Preprocessor.super.visitCast(
        bindingPoint.type(),
        rawBindingExpression(bindingPoint.name()).invoke("getObject"));
  }

  @Override
  public void visitNamed(String name) {
    if (name.equals(OBJECT_VALUE)) {
      visitor().visitCast(context.typeBefore(), v -> v.visitNamed(OBJECT_VALUE));

    } else if (name.equals(PARENT_VALUE)) {
      throw new UnsupportedOperationException("PARENT not supported yet");

    } else if (name.startsWith(BOUND_PREFIX)) {
      name = name.substring(BOUND_PREFIX.length());
      visitBoundExpression(context.getChild(name));

    } else {
      Preprocessor.super.visitNamed(name);
    }
  }

  @Override
  public void visitNamedAssignment(String name, Expression value) {
    if (name.equals(OBJECT_VALUE)) {
      visitor().visitNamedAssignment(OBJECT_VALUE, process(value.check(context.typeBefore())));

    } else if (name.equals(PARENT_VALUE)) {
      throw new UnsupportedOperationException("PARENT not supported yet");

    } else if (name.startsWith(BOUND_PREFIX)) {
      throw new ModabiSchemaException(MESSAGES.cannotAssignToBoundObject());

    } else {
      Preprocessor.super.visitNamedAssignment(name, v -> value.evaluate(this));
    }
  }
}
