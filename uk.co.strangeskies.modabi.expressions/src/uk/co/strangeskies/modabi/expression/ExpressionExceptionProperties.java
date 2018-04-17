package uk.co.strangeskies.modabi.expression;

import java.lang.reflect.Type;

import uk.co.strangeskies.reflection.token.TypeToken;

public interface ExpressionExceptionProperties {
  String typeMustBeFunctionalInterface(Type implementationType);

  String cannotPerformCast(TypeToken<?> to, TypeToken<?> from);

  String cannotPerformAssignment(TypeToken<?> to, TypeToken<?> from);

  String cannotResolveField(TypeToken<?> type, String variable);

  String cannotResolveStaticField(Class<?> type, String variable);

  String expressionIsAlreadyCompleted();

  String expressionIsNotCompleted();

  String illegalLiteralType(Class<?> type);
}
