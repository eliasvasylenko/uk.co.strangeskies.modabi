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

public interface ValueExpression {
  void evaluate(ExpressionVisitor visitor);

  default VariableExpression getField(String field) {
    return new VariableExpression() {
      @Override
      public void evaluate(ExpressionVisitor visitor) {
        visitor.visitGetField(ValueExpression.this, field);
      }

      @Override
      public ValueExpression assign(ValueExpression value) {
        return v -> v.visitSetField(ValueExpression.this, field, value);
      }
    };
  }

  default ValueExpression invoke(String methodName, ValueExpression... arguments) {
    return invoke(methodName, asList(arguments));
  }

  default <R> ValueExpression invoke(String methodName, List<ValueExpression> arguments) {
    return v -> v.visitInvocation(this, methodName, arguments);
  }

  /**
   * Create an expression over a number of iterable items. The returned expression
   * conceptually represents the assigned variable in a for each loop over an
   * iterable.
   * 
   * <p>
   * In the case that a result of invocation of this method is passed to
   * {@link #expression(ValueExpression)}, the output items will be iterated over
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
  default ValueExpression iterate() {
    return v -> v.visitIteration(this);
  }
}
