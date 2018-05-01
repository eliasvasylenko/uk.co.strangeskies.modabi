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

/**
 * Expressions are designed to be decoupled from the mechanism of evaluation or
 * compilation as there are a number of different strategies which could be
 * employed with various performance and API tradeoffs.
 * <p>
 * Typically expressions are evaluated or compiled according to the Java
 * language specification, when dealing with e.g. override resolution and type
 * inference. Where forms of expressions don't match one-to-one with forms
 * allowed by the Java language specification, differences in behavior should be
 * documented in {@link ExpressionVisitor} on the relevant method.
 * <p>
 * Compiler implementations may choose to provide information about the types
 * which were inferred during compilation of an expression.
 * <p>
 * The {@link #evaluate(ExpressionVisitor)} method of an expression should only
 * invoke methods on the given visitor and should otherwise be idempotent and
 * have no side-effects.
 * 
 * @author Elias N Vasylenko
 */
public interface Expression {
  Instructions compile(Scope scope);

  @Override
  String toString();

  default MutableExpression getField(String field) {
    return new FieldExpression(this, field);
  }

  default Expression cast(TypeToken<?> type) {
    return new CastExpression(this, type);
  }

  default Expression invoke(String methodName, Expression... arguments) {
    return invoke(methodName, asList(arguments));
  }

  default <R> Expression invoke(String methodName, List<Expression> arguments) {
    return new MethodInvocationExpression(this, methodName, arguments);
  }

  /**
   * Create an expression over a number of iterable items. The returned expression
   * conceptually represents the assigned variable in a for each loop over an
   * iterable.
   * 
   * <p>
   * In the case that a result of invocation of this method is passed to
   * {@link #expression(Expression)}, the output items will be iterated over
   * accordingly.
   * 
   * <p>
   * Expressions returned from this method may then be mentioned by expressions
   * passed to this method, creating nested iterations. Conceptually these will
   * correspond to nested for loops in the output logic.
   * 
   * @param values
   *          an expression over an iterable object instance
   * @return an inner-loop expression over the items of an iterable object
   *         instance
   */
  default Expression iterate() {
    return new IterationExpression(this);
  }
}
