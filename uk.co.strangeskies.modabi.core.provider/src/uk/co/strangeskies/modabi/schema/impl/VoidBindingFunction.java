package uk.co.strangeskies.modabi.schema.impl;

import static uk.co.strangeskies.modabi.expression.Expressions.voidExpression;
import static uk.co.strangeskies.reflection.token.TypedObject.typedObject;

import uk.co.strangeskies.modabi.expression.Expression;
import uk.co.strangeskies.modabi.schema.BindingContext;
import uk.co.strangeskies.modabi.schema.BindingFunction;
import uk.co.strangeskies.reflection.token.TypeToken;
import uk.co.strangeskies.reflection.token.TypedObject;

public class VoidBindingFunction implements BindingFunction {
  private final TypeToken<?> objectType;

  public VoidBindingFunction(TypeToken<?> objectType) {
    this.objectType = objectType;
  }

  @Override
  public TypeToken<?> getTypeBefore() {
    return objectType;
  }

  @Override
  public TypeToken<?> getTypeAfter() {
    return objectType;
  }

  @Override
  public Expression getExpression() {
    return voidExpression();
  }

  @Override
  public TypedObject<?> apply(BindingContext context) {
    return typedObject(void.class, null);
  }
}
