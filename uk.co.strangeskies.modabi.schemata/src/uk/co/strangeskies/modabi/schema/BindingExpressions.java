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
package uk.co.strangeskies.modabi.schema;

import static uk.co.strangeskies.modabi.expression.Expressions.invokeNamed;
import static uk.co.strangeskies.modabi.expression.Expressions.named;
import static uk.co.strangeskies.modabi.expression.Expressions.typeToken;
import static uk.co.strangeskies.reflection.token.TypeToken.forType;

import java.lang.reflect.Type;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.expression.Expression;
import uk.co.strangeskies.modabi.expression.MutableExpression;
import uk.co.strangeskies.reflection.token.TypeToken;

public class BindingExpressions {
  public static final String TARGET_VALUE = "target";
  public static final String RESULT_VALUE = "result";
  public static final String SOURCE_VALUE = "source";
  public static final String PARENT_VALUE = "parent";
  public static final String PROVIDE_METHOD = "provide";
  public static final String BOUND_PREFIX = "$";
  public static final String BINDING_PREFIX = "%";
  public static final String BINDING_POINT_PREFIX = "@";

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

  public static MutableExpression target() {
    return named(TARGET_VALUE);
  }

  public static Expression result() {
    return named(RESULT_VALUE);
  }

  public static Expression source() {
    return named(SOURCE_VALUE);
  }

  public static Expression parent() {
    return named(PARENT_VALUE);
  }

  public static Expression boundValue(String bindingPoint) {
    return named(BOUND_PREFIX + bindingPoint);
  }

  public static Expression boundValue(QualifiedName bindingPoint) {
    return named(BOUND_PREFIX + bindingPoint);
  }

  public static Expression binding(String bindingPoint) {
    return named(BINDING_PREFIX + bindingPoint);
  }

  public static Expression binding(QualifiedName bindingPoint) {
    return named(BINDING_PREFIX + bindingPoint);
  }

  public static Expression bindingPoint(String bindingPoint) {
    return named(BINDING_POINT_PREFIX + bindingPoint);
  }

  public static Expression bindingPoint(QualifiedName bindingPoint) {
    return named(BINDING_POINT_PREFIX + bindingPoint);
  }
}
