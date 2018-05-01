package uk.co.strangeskies.modabi.expression;

import static uk.co.strangeskies.modabi.expression.ExpressionException.MESSAGES;
import static uk.co.strangeskies.reflection.ConstraintFormula.Kind.LOOSE_COMPATIBILILTY;
import static uk.co.strangeskies.reflection.token.FieldToken.staticFields;
import static uk.co.strangeskies.reflection.token.VariableMatcher.anyVariable;

import uk.co.strangeskies.reflection.token.FieldToken;

public class StaticFieldAssignmentExpression implements Expression {
  private final Class<?> type;
  private final String fieldName;
  private final Expression value;

  StaticFieldAssignmentExpression(Class<?> type, String fieldName, Expression value) {
    this.type = type;
    this.fieldName = fieldName;
    this.value = value;
  }

  @Override
  public Instructions compile(Scope scope) {
    Instructions valueInstructions = value.compile(scope);

    FieldToken<Void, ?> field = staticFields(type)
        .filter(anyVariable().named(fieldName))
        .findAny()
        .orElseThrow(
            () -> new ExpressionException(MESSAGES.cannotResolveStaticField(type, fieldName)));

    if (!field
        .getFieldType()
        .satisfiesConstraintFrom(LOOSE_COMPATIBILILTY, valueInstructions.getResultType()))
      throw new ExpressionException(
          MESSAGES
              .cannotPerformAssignment(field.getFieldType(), valueInstructions.getResultType()));

    return new Instructions(field.getFieldType(), v -> v.putStatic(field, valueInstructions));
  }

  @Override
  public String toString() {
    return "(" + type + "." + fieldName + " = " + value.toString() + ")";
  }
}
