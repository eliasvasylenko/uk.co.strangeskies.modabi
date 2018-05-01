package uk.co.strangeskies.modabi.expression;

import static uk.co.strangeskies.modabi.expression.ExpressionException.MESSAGES;
import static uk.co.strangeskies.reflection.token.VariableMatcher.anyVariable;

import uk.co.strangeskies.reflection.token.FieldToken;

class FieldExpression implements MutableExpression {
  private final Expression receiver;
  private final String fieldName;

  FieldExpression(Expression receiver, String fieldName) {
    this.receiver = receiver;
    this.fieldName = fieldName;
  }

  @Override
  public Instructions compile(Scope scope) {
    Instructions receiverInstructions = receiver.compile(scope);

    FieldToken<?, ?> field = receiverInstructions
        .getResultType()
        .fields()
        .filter(anyVariable().named(fieldName))
        .findAny()
        .orElseThrow(
            () -> new ExpressionException(
                MESSAGES.cannotResolveField(receiverInstructions.getResultType(), fieldName)));

    return new Instructions(field.getFieldType(), v -> v.getMember(receiverInstructions, field));
  }

  @Override
  public Expression assign(Expression value) {
    return new FieldAssignmentExpression(receiver, fieldName, value);
  }

  @Override
  public String toString() {
    return receiver + "." + fieldName;
  }
}
