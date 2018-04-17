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

import static uk.co.strangeskies.modabi.expression.Expressions.invokeStatic;
import static uk.co.strangeskies.modabi.expression.Expressions.literal;
import static uk.co.strangeskies.reflection.ParameterizedTypes.getAllTypeArguments;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import uk.co.strangeskies.reflection.ParameterizedTypes;
import uk.co.strangeskies.reflection.WildcardTypes;

class TypeExpression implements Expression {
  private final Expression expression;

  public TypeExpression(Type type) {
    if (type instanceof Class<?>) {
      expression = literal((Class<?>) type);

    } else if (type instanceof ParameterizedType) {
      ParameterizedType parameterizedType = (ParameterizedType) type;

      List<Expression> arguments = new ArrayList<>();
      arguments.add(literal((Class<?>) parameterizedType.getRawType()));
      getAllTypeArguments(parameterizedType)
          .map(Entry::getValue)
          .map(TypeExpression::new)
          .forEach(arguments::add);

      expression = invokeStatic(ParameterizedTypes.class, "parameterizeUnchecked", arguments);

    } else if (type instanceof WildcardType) {
      WildcardType wildcardType = (WildcardType) type;

      if (wildcardType.getLowerBounds().length > 0) {
        List<Expression> arguments = new ArrayList<>();
        Arrays
            .stream(wildcardType.getLowerBounds())
            .map(TypeExpression::new)
            .forEach(arguments::add);

        expression = invokeStatic(WildcardTypes.class, "wildcardSuper", arguments);

      } else {
        List<Expression> arguments = new ArrayList<>();
        Arrays
            .stream(wildcardType.getUpperBounds())
            .map(TypeExpression::new)
            .forEach(arguments::add);

        expression = invokeStatic(WildcardTypes.class, "wildcardExtending", arguments);
      }

    } else {
      throw new IllegalArgumentException("Unsupported type " + type);
    }
  }

  @Override
  public void evaluate(ExpressionVisitor visitor) {
    expression.evaluate(visitor);
  }
}
