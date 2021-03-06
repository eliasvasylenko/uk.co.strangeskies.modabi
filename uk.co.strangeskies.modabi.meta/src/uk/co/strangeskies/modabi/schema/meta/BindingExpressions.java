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
package uk.co.strangeskies.modabi.schema.meta;

import static uk.co.strangeskies.modabi.expression.Expressions.invokeNamed;
import static uk.co.strangeskies.modabi.expression.Expressions.invokeStatic;
import static uk.co.strangeskies.modabi.expression.Expressions.named;
import static uk.co.strangeskies.modabi.expression.Expressions.typeToken;
import static uk.co.strangeskies.reflection.token.TypeToken.forType;

import java.lang.reflect.Type;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.expression.Expression;
import uk.co.strangeskies.modabi.expression.Expressions;
import uk.co.strangeskies.modabi.expression.MutableExpression;
import uk.co.strangeskies.reflection.token.TypeToken;

public class BindingExpressions {
  public static final String OBJECT_VALUE = "object";
  public static final String PROVIDE_METHOD = "provide";
  public static final String BOUND_PREFIX = "$";

  private BindingExpressions() {}

  public static Expression provide() {
    return invokeNamed(PROVIDE_METHOD);
  }

  public static Expression provide(Type type) {
    return provide(forType(type));
  }

  public static Expression provide(TypeToken<?> typeToken) {
    return invokeNamed(PROVIDE_METHOD, typeToken(typeToken));
  }

  public static MutableExpression object() {
    return named(OBJECT_VALUE);
  }

  public static Expression boundValue() {
    return named(BOUND_PREFIX);
  }

  public static Expression boundValue(String bindingPoint) {
    return named(BOUND_PREFIX + bindingPoint);
  }

  public static Expression boundValue(QualifiedName bindingPoint) {
    return named(BOUND_PREFIX + bindingPoint);
  }

  public static Expression qualifiedName(QualifiedName bindingPoint) {
    return invokeStatic(
        QualifiedName.class,
        "parseString",
        Expressions.literal(bindingPoint.toString()));
  }
}
