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

import static uk.co.strangeskies.modabi.expression.Expressions.invokeConstructor;
import static uk.co.strangeskies.modabi.expression.Expressions.literal;
import static uk.co.strangeskies.modabi.expression.Expressions.tryLiteral;
import static uk.co.strangeskies.reflection.Annotations.getModifiedProperties;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import uk.co.strangeskies.reflection.AnnotationProperty;
import uk.co.strangeskies.reflection.Annotations;

public class AnnotationExpression implements Expression {
  private final Expression expression;

  public AnnotationExpression(Annotation annotation) {
    List<Expression> arguments = new ArrayList<>();

    arguments.add(literal(annotation.annotationType()));

    getModifiedProperties(annotation).forEach(property -> {
      Expression value;
      if (property.value() instanceof Annotation) {
        value = new AnnotationExpression((Annotation) property.value());
      } else {
        value = tryLiteral(property.value());
      }

      arguments.add(invokeConstructor(AnnotationProperty.class, literal(property.name()), value));
    });

    expression = invokeConstructor(Annotations.class, arguments);
  }

  @Override
  public void evaluate(ExpressionVisitor visitor) {
    expression.evaluate(visitor);
  }
}
