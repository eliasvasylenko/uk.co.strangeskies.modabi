/*
 * Copyright (C) 2017 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
 *      __   _______  ____           _       __     _      __       __
 *    ,`_ `,|__   __||  _ `.        / \     |  \   | |  ,-`__`¬  ,-`__`¬
 *   ( (_`-'   | |   | | ) |       / . \    | . \  | | / .`  `' / .`  `'
 *    `._ `.   | |   | |<. L      / / \ \   | |\ \ | || |    _ | '--.
 *   _   `. \  | |   | |  `.`.   / /   \ \  | | \ \| || |   | || +--'
 *  \ \__.' /  | |   | |    \ \ / /     \ \ | |  \ ` | \ `._' | \ `.__,.
 *   `.__.-`   |_|   |_|    |_|/_/       \_\|_|   \__|  `-.__.J  `-.__.J
 *                   __    _         _      __      __
 *                 ,`_ `, | |  _    | |  ,-`__`¬  ,`_ `,
 *                ( (_`-' | | ) |   | | / .`  `' ( (_`-'
 *                 `._ `. | L-' L   | || '--.     `._ `.
 *                _   `. \| ,.-^.`. | || +--'    _   `. \
 *               \ \__.' /| |    \ \| | \ `.__,.\ \__.' /
 *                `.__.-` |_|    |_||_|  `-.__.J `.__.-`
 *
 * This file is part of uk.co.strangeskies.reflection.codegen.
 *
 * uk.co.strangeskies.reflection.codegen is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.reflection.codegen is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi.expression;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;

import uk.co.strangeskies.reflection.token.ExecutableToken;
import uk.co.strangeskies.reflection.token.TypeArgument;
import uk.co.strangeskies.reflection.token.TypeToken;

public final class Expressions {
  private Expressions() {}

  public static Expression nullLiteral() {
    return new NullLiteralExpression();
  }

  public static Expression tryLiteral(Object value) {
    if (value instanceof String)
      return literal((String) value);

    else if (value instanceof Integer)
      return literal((int) value);

    else if (value instanceof Long)
      return literal((long) value);

    else if (value instanceof Float)
      return literal((float) value);

    else if (value instanceof Double)
      return literal((double) value);

    else if (value instanceof Character)
      return literal((char) value);

    else if (value instanceof Class<?>)
      return literal((Class<?>) value);

    throw new IllegalArgumentException(value.toString());
  }

  public static Expression literal(String value) {
    return new StringLiteralExpression(value);
  }

  public static Expression literal(int value) {
    return new IntLiteralExpression(value);
  }

  public static Expression literal(long value) {
    return new LongLiteralExpression(value);
  }

  public static Expression literal(float value) {
    return new FloatLiteralExpression(value);
  }

  public static Expression literal(double value) {
    return new DoubleLiteralExpression(value);
  }

  public static Expression literal(char value) {
    return new CharLiteralExpression(value);
  }

  public static <T> Expression literal(Class<T> value) {
    return new ClassLiteralExpression(value);
  }

  public static MutableExpression getStaticField(Class<?> declaringClass, String fieldName) {
    return new StaticFieldExpression(declaringClass, fieldName);
  }

  public static Expression invokeConstructor(Class<?> declaringClass, Expression... arguments) {
    return invokeConstructor(declaringClass, asList(arguments));
  }

  /**
   * @param <T>
   *          the type of the result of the execution
   * @param executable
   *          the executable to be invoked
   * @param arguments
   *          the expressions of the arguments of the invocation
   * @return an expression describing the invocation of the given static
   *         executable with the given argument expressions
   */
  public static Expression invokeConstructor(Class<?> declaringClass, List<Expression> arguments) {
    return new ConstructorInvocationExpression(declaringClass, arguments);
  }

  public static Expression invokeStatic(
      Class<?> declaringClass,
      String methodName,
      Expression... arguments) {
    return invokeStatic(declaringClass, methodName, asList(arguments));
  }

  /**
   * @param <T>
   *          the type of the result of the execution
   * @param executable
   *          the executable to be invoked
   * @param arguments
   *          the expressions of the arguments of the invocation
   * @return an expression describing the invocation of the given static
   *         executable with the given argument expressions
   */
  public static Expression invokeStatic(
      Class<?> declaringClass,
      String methodName,
      List<Expression> arguments) {
    return new StaticMethodInvocationExpression(declaringClass, methodName, arguments);
  }

  public static Expression invokeNamed(String methodName, Expression... arguments) {
    return invokeNamed(methodName, asList(arguments));
  }

  public static Expression invokeNamed(String methodName, List<Expression> arguments) {
    return new NamedInvocationExpression(methodName, arguments);
  }

  public static MutableExpression named(String variableName) {
    return new NamedExpression(variableName);
  }

  public static Expression typeToken(TypeToken<?> token) {
    return new CastExpression(
        invokeStatic(
            TypeToken.class,
            "forAnnotatedType",
            new AnnotatedTypeExpression(token.getAnnotatedDeclaration())),
        getTypeTokenType(token));
  }

  private static <T> TypeToken<TypeToken<T>> getTypeTokenType(TypeToken<T> token) {
    return new TypeToken<TypeToken<T>>() {}.withTypeArguments(new TypeArgument<T>(token) {});
  }

  public static Expression voidExpression() {
    return new VoidExpression();
  }

  public static List<Instructions> argumentInstructionSequence(
      ExecutableToken<?, ?> executable,
      List<Instructions> argumentInstructions) {

    if (!executable.isVariableArityInvocation()) {
      return argumentInstructions;

    } else {
      int argumentCount = argumentInstructions.size();
      int parameterCount = (int) executable.getParameters().count();

      ArrayList<Instructions> instructionSequence = new ArrayList<>(parameterCount);

      for (int i = 0; i < parameterCount - 1; i++) {
        instructionSequence.add(argumentInstructions.get(i));
      }

      TypeToken<?> arrayType = executable.getParameters().reduce((a, b) -> b).get().getTypeToken();

      instructionSequence
          .add(
              new Instructions(
                  arrayType,
                  v -> v
                      .newArray(
                          arrayType,
                          argumentInstructions.subList(parameterCount, argumentCount))));

      return instructionSequence;
    }
  }
}
