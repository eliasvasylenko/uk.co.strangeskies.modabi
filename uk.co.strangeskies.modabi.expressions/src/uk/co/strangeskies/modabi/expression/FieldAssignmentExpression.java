package uk.co.strangeskies.modabi.expression;

import static uk.co.strangeskies.modabi.expression.ExpressionException.MESSAGES;
import static uk.co.strangeskies.reflection.ConstraintFormula.Kind.LOOSE_COMPATIBILILTY;
import static uk.co.strangeskies.reflection.token.VariableMatcher.anyVariable;

import uk.co.strangeskies.reflection.token.FieldToken;

public class FieldAssignmentExpression implements Expression {
  private final Expression receiver;
  private final String fieldName;
  private final Expression value;

  FieldAssignmentExpression(Expression receiver, String fieldName, Expression value) {
    this.receiver = receiver;
    this.fieldName = fieldName;
    this.value = value;
  }

  @Override
  public Instructions compile(Scope scope) {
    Instructions receiverInstructions = receiver.compile(scope);
    Instructions valueInstructions = value.compile(scope);

    FieldToken<?, ?> field = receiverInstructions
        .getResultType()
        .fields()
        .filter(anyVariable().named(fieldName))
        .findAny()
        .orElseThrow(
            () -> new ExpressionException(
                MESSAGES.cannotResolveField(receiverInstructions.getResultType(), fieldName)));

    if (!field
        .getFieldType()
        .satisfiesConstraintFrom(LOOSE_COMPATIBILILTY, valueInstructions.getResultType()))
      throw new ExpressionException(
          MESSAGES
              .cannotPerformAssignment(field.getFieldType(), valueInstructions.getResultType()));

    return new Instructions(
        field.getFieldType(),
        v -> v.putMember(receiverInstructions, field, valueInstructions));
  }

  @Override
  public String toString() {
    return "(" + receiver + "." + fieldName + " = " + value.toString() + ")";
  }
}
