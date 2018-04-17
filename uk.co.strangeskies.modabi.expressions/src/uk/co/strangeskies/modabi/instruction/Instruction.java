package uk.co.strangeskies.modabi.instruction;

public interface Instruction {
  void visit(InstructionVisitor visitor);
}
