package uk.co.strangeskies.modabi.instruction;

import uk.co.strangeskies.reflection.token.ExecutableToken;
import uk.co.strangeskies.reflection.token.FieldToken;
import uk.co.strangeskies.reflection.token.TypeToken;

public interface InstructionVisitor {
  void visitMemberInvocation(ExecutableToken<?, ?> method);

  void visitStaticInvocation(ExecutableToken<?, ?> method);

  void visitConstructorInvocation(ExecutableToken<?, ?> method);

  void visitNamedInvocation(String name, int arguments);

  void visitGetField(FieldToken<?, ?> field);

  void visitGetStaticField(FieldToken<?, ?> field);

  void visitGetNamed(String name);

  void visitPutField(FieldToken<?, ?> field);

  void visitPutStaticField(FieldToken<?, ?> field);

  void visitPutNamed(String name);

  void visitNull();

  void visitCapture();

  void newArray(TypeToken<?> type);

  void visitLiteral(int value);

  void visitLiteral(long value);

  void visitLiteral(float value);

  void visitLiteral(double value);

  void visitLiteral(String value);

  /**
   * Push the given literal onto the stack.
   * 
   * @param value
   */
  void visitLiteral(Class<?> value);

  /**
   * Duplicate the item at the top of the stack.
   */
  void visitDuplicate();

  void visitArrayStore();

  void visitIterate();
}
