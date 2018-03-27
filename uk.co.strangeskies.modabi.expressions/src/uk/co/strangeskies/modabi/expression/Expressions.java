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

import java.util.List;

import uk.co.strangeskies.reflection.token.TypeToken;

public class Expressions {
  private Expressions() {}

  public static Expression nullLiteral() {
    return v -> v.visitNull();
  }

  private static Expression literalImpl(Object value) {
    return v -> v.visitLiteral(value);
  }

  public static Expression tryLiteral(Object object) {
    if (object instanceof String || object instanceof Integer || object instanceof Float
        || object instanceof Long || object instanceof Double || object instanceof Byte
        || object instanceof Character || object instanceof Class<?>) {
      return literalImpl(object);
    }
    throw new IllegalArgumentException();
  }

  public static Expression literal(String value) {
    return literalImpl(value);
  }

  public static Expression literal(int value) {
    return literalImpl(value);
  }

  public static Expression literal(float value) {
    return literalImpl(value);
  }

  public static Expression literal(long value) {
    return literalImpl(value);
  }

  public static Expression literal(double value) {
    return literalImpl(value);
  }

  public static Expression literal(byte value) {
    return literalImpl(value);
  }

  public static Expression literal(char value) {
    return literalImpl(value);
  }

  public static <T> Expression literal(Class<T> value) {
    return literalImpl(value);
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
    return v -> v.visitConstructorInvocation(declaringClass, arguments);
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
    return v -> v.visitStaticInvocation(declaringClass, methodName, arguments);
  }

  public static Expression invokeNamed(String methodName, Expression... arguments) {
    return invokeNamed(methodName, asList(arguments));
  }

  public static Expression invokeNamed(String methodName, List<Expression> arguments) {
    return v -> v.visitNamedInvocation(methodName, arguments);
  }

  public static MutableExpression named(String name) {
    return new MutableExpression() {
      @Override
      public void evaluate(ExpressionVisitor visitor) {
        visitor.visitNamed(name);
      }

      @Override
      public Expression assign(Expression value) {
        return v -> v.visitNamedAssignment(name, value);
      }
    };
  }

  public static Expression typeToken(TypeToken<?> token) {
    return v -> v
        .visitCast(
            token.getThisTypeToken(),
            invokeStatic(
                TypeToken.class,
                "forAnnotatedType",
                new AnnotatedTypeExpression(token.getAnnotatedDeclaration())));
  }
}
