package uk.co.strangeskies.modabi.schema.impl.bindingfunctions;

import static java.util.Arrays.asList;
import static uk.co.strangeskies.modabi.expression.Expressions.literal;
import static uk.co.strangeskies.modabi.schema.BindingExpressions.BOUND_PREFIX;
import static uk.co.strangeskies.modabi.schema.BindingExpressions.OBJECT_VALUE;
import static uk.co.strangeskies.modabi.schema.ModabiSchemaException.MESSAGES;

import uk.co.strangeskies.modabi.expression.Expression;
import uk.co.strangeskies.modabi.expression.ExpressionVisitor;
import uk.co.strangeskies.modabi.expression.Preprocessor;
import uk.co.strangeskies.modabi.schema.Child;
import uk.co.strangeskies.modabi.schema.ModabiSchemaException;
import uk.co.strangeskies.reflection.token.TypeToken;

public class BindingFunctionPreprocessor implements Preprocessor {
  private final ExpressionVisitor expressionVisitor;
  private final ChildLookup childLookup;
  private final TypeToken<?> objectType;
  private final TypeToken<?> objectAssignedType;

  public BindingFunctionPreprocessor(
      ExpressionVisitor expressionVisitor,
      ChildLookup context,
      TypeToken<?> objectType,
      TypeToken<?> objectAssignedType) {
    this.expressionVisitor = expressionVisitor;
    this.childLookup = context;
    this.objectType = objectType;
    this.objectAssignedType = objectAssignedType;
  }

  @Override
  public ExpressionVisitor visitor() {
    return expressionVisitor;
  }

  private Expression rawBindingExpression(int bindingPoint) {
    return v -> v
        .visitInvocation(
            c -> c.visitNamed("context"),
            "getBoundObject",
            asList(literal(bindingPoint)));
  }

  private void visitBoundExpression(Child<?> bindingPoint) {
    Preprocessor.super.visitCast(
        bindingPoint.type(),
        rawBindingExpression(bindingPoint.index()).invoke("getObject"));
  }

  @Override
  public void visitNamed(String name) {
    if (name.equals(OBJECT_VALUE)) {
      visitor().visitCast(objectType, v -> v.visitNamed(OBJECT_VALUE));

    } else if (name.startsWith(BOUND_PREFIX)) {
      String boundName = name.substring(BOUND_PREFIX.length());
      visitBoundExpression(
          childLookup
              .getChild(boundName)
              .orElseThrow(() -> new ModabiSchemaException(MESSAGES.cannotResolveVariable(name))));

    } else {
      Preprocessor.super.visitNamed(name);
    }
  }

  @Override
  public void visitNamedAssignment(String name, Expression value) {
    if (name.equals(OBJECT_VALUE)) {
      visitor().visitNamedAssignment(OBJECT_VALUE, process(value.check(objectAssignedType)));

    } else if (name.startsWith(BOUND_PREFIX)) {
      throw new ModabiSchemaException(MESSAGES.cannotAssignToBoundObject());

    } else {
      Preprocessor.super.visitNamedAssignment(name, v -> value.evaluate(this));
    }
  }
}
