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

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static uk.co.strangeskies.modabi.schema.expression.Expressions.invokeStatic;
import static uk.co.strangeskies.modabi.schema.expression.Expressions.literal;
import static uk.co.strangeskies.reflection.token.MethodMatcher.anyMethod;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import uk.co.strangeskies.reflection.AnnotatedTypes;

class AnnotatedTypeExpression implements ValueExpression<AnnotatedType> {
  private final ValueExpression<AnnotatedType> expression;

  public AnnotatedTypeExpression(AnnotatedType annotatedType) {
    Type type = annotatedType.getType();

    if (type instanceof Class<?>) {
      List<ValueExpression<?>> arguments = new ArrayList<>();
      arguments.add(literal((Class<?>) type));
      arguments.addAll(getAnnotationExpressions(annotatedType.getAnnotations()));

      expression = invokeStatic(AnnotatedTypes.class, anyMethod().named("annotated"), arguments);
    }

    throw new IllegalArgumentException();
  }

  private List<ValueExpression<Annotation>> getAnnotationExpressions(Annotation[] annotations) {
    return stream(annotations).map(AnnotationExpression::new).collect(toList());
  }

  @Override
  public void evaluate(ExpressionVisitor visitor) {
    expression.evaluate(visitor);
  }
}
