package uk.co.strangeskies.modabi.expression;

import static uk.co.strangeskies.modabi.expression.ExpressionException.MESSAGES;
import static uk.co.strangeskies.reflection.token.FieldToken.staticFields;
import static uk.co.strangeskies.reflection.token.VariableMatcher.anyVariable;

import uk.co.strangeskies.reflection.token.FieldToken;

class StaticFieldExpression implements MutableExpression {
  private final Class<?> type;
  private final String fieldName;

  public StaticFieldExpression(Class<?> type, String fieldName) {
    this.type = type;
    this.fieldName = fieldName;
  }

  @Override
  public Instructions compile(Scope scope) {
    FieldToken<?, ?> field = staticFields(type)
        .filter(anyVariable().named(fieldName))
        .findAny()
        .orElseThrow(
            () -> new ExpressionException(MESSAGES.cannotResolveStaticField(type, fieldName)));

    return new Instructions(field.getFieldType(), v -> v.getStatic(field));
  }

  @Override
  public Expression assign(Expression value) {
    return new StaticFieldAssignmentExpression(type, fieldName, value);
  }

  @Override
  public String toString() {
    return type + "." + fieldName;
  }
}
