package uk.co.strangeskies.modabi.expression;

import java.util.List;

import uk.co.strangeskies.reflection.token.ExecutableToken;
import uk.co.strangeskies.reflection.token.FieldToken;
import uk.co.strangeskies.reflection.token.TypeToken;

public interface InstructionVisitor {
  void invokeMember(
      Instructions receiver,
      ExecutableToken<?, ?> method,
      List<Instructions> arguments);

  void invokeStatic(ExecutableToken<?, ?> method, List<Instructions> arguments);

  void invokeConstructor(ExecutableToken<?, ?> method, List<Instructions> arguments);

  void invokeNamed(String name, List<Instructions> arguments);

  void getMember(Instructions receiver, FieldToken<?, ?> field);

  void getStatic(FieldToken<?, ?> field);

  void getNamed(String name);

  void putMember(Instructions receiver, FieldToken<?, ?> field, Instructions value);

  void putStatic(FieldToken<?, ?> field, Instructions value);

  void putNamed(String name, Instructions value);

  void newArray(TypeToken<?> type, int size);

  void newArray(TypeToken<?> type, List<Instructions> instructions);

  void nullLiteral();

  void intLiteral(int value);

  void longLiteral(long value);

  void floatLiteral(float value);

  void doubleLiteral(double value);

  void stringLiteral(String value);

  void classLiteral(Class<?> value);

  void iterate(Instructions instructions);

  void cast(TypeToken<?> resultType, Instructions value);
}
