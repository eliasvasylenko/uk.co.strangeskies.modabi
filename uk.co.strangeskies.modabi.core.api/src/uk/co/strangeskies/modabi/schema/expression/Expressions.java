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
package uk.co.strangeskies.modabi.schema.expression;

import static java.util.Arrays.asList;

import java.util.List;

import uk.co.strangeskies.reflection.token.TypeToken;

public class Expressions {
  private Expressions() {}

  public static ValueExpression receiver(TypeToken<?> token) {
    return v -> v.visitReceiver();
  }

  public static ValueExpression receiver(Class<?> token) {
    return v -> v.visitReceiver();
  }

  public static ValueExpression nullLiteral() {
    return v -> v.visitNull();
  }

  private static ValueExpression literalImpl(Object value) {
    return v -> v.visitLiteral(value);
  }

  public static ValueExpression tryLiteral(Object object) {
    if (object instanceof String || object instanceof Integer || object instanceof Float
        || object instanceof Long || object instanceof Double || object instanceof Byte
        || object instanceof Character || object instanceof Class<?>) {
      return literalImpl(object);
    }
    throw new IllegalArgumentException();
  }

  public static ValueExpression literal(String value) {
    return literalImpl(value);
  }

  public static ValueExpression literal(int value) {
    return literalImpl(value);
  }

  public static ValueExpression literal(float value) {
    return literalImpl(value);
  }

  public static ValueExpression literal(long value) {
    return literalImpl(value);
  }

  public static ValueExpression literal(double value) {
    return literalImpl(value);
  }

  public static ValueExpression literal(byte value) {
    return literalImpl(value);
  }

  public static ValueExpression literal(char value) {
    return literalImpl(value);
  }

  public static <T> ValueExpression literal(Class<T> value) {
    return literalImpl(value);
  }

  public static ValueExpression invokeConstructor(
      Class<?> declaringClass,
      ValueExpression... arguments) {
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
  public static ValueExpression invokeConstructor(
      Class<?> declaringClass,
      List<ValueExpression> arguments) {
    return v -> v.visitConstructorInvocation(declaringClass, arguments);
  }

  public static ValueExpression invokeStatic(
      Class<?> declaringClass,
      String methodName,
      ValueExpression... arguments) {
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
  public static ValueExpression invokeStatic(
      Class<?> declaringClass,
      String methodName,
      List<ValueExpression> arguments) {
    return v -> v.visitStaticInvocation(declaringClass, methodName, arguments);
  }

  public static ValueExpression typeToken(TypeToken<?> token) {
    return v -> v.visitCast(
        token.getThisTypeToken(),
        invokeStatic(
            TypeToken.class,
            "forType",
            new AnnotatedTypeExpression(token.getAnnotatedDeclaration())));
  }
}
