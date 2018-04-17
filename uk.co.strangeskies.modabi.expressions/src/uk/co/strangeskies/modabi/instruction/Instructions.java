package uk.co.strangeskies.modabi.instruction;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import uk.co.strangeskies.reflection.token.TypeToken;

/**
 * A sequence of instructions which does not consume any existing stack and
 * optionally leaves a single item on the stack when complete.
 * 
 * @author Elias N Vasylenko
 */
public class Instructions implements Instruction {
  private final TypeToken<?> resultType;
  private final List<Instruction> instructions;

  public Instructions(TypeToken<?> resultType, Instruction... instructions) {
    this(resultType, asList(instructions));
  }

  public Instructions(TypeToken<?> resultType, List<Instruction> instructions) {
    this.resultType = resultType;
    this.instructions = new ArrayList<>(instructions);
  }

  public Stream<Instruction> instructionSequence() {
    return instructions.stream();
  }

  public Stream<Instruction> flatInstructionSequence() {
    return instructionSequence()
        .flatMap(
            i -> i instanceof Instructions
                ? ((Instructions) i).instructionSequence()
                : Stream.of(i));
  }

  /**
   * @return the type of the remaining stack item, or {@link Void void} if this is
   *         a void expression
   */
  public TypeToken<?> resultType() {
    return resultType;
  }

  @Override
  public void visit(InstructionVisitor visitor) {
    instructionSequence().forEach(i -> i.visit(visitor));
  }
}
