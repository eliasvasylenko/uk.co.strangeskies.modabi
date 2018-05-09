package uk.co.strangeskies.modabi.expression;

import static java.util.Objects.requireNonNull;

import java.util.function.Consumer;

import uk.co.strangeskies.reflection.token.TypeToken;

public class Instructions {
  private final TypeToken<?> resultType;
  private final Consumer<InstructionVisitor> action;

  public Instructions(TypeToken<?> resultType, Consumer<InstructionVisitor> action) {
    this.resultType = requireNonNull(resultType);
    this.action = requireNonNull(action);
  }

  public void visit(InstructionVisitor visitor) {
    action.accept(visitor);
  }

  public TypeToken<?> getResultType() {
    return resultType;
  }
}
