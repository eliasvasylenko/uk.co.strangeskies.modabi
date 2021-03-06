/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.core.api.
 *
 * uk.co.strangeskies.modabi.core.api is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.core.api is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.core.api.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi.binding;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import uk.co.strangeskies.mathematics.Interval;
import uk.co.strangeskies.modabi.ModabiProperties;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.expression.Expression;
import uk.co.strangeskies.modabi.schema.BindingPoint;
import uk.co.strangeskies.modabi.schema.Child;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.reflection.token.TypeToken;
import uk.co.strangeskies.text.properties.PropertyConfiguration;
import uk.co.strangeskies.text.properties.PropertyConfiguration.KeyCase;

@PropertyConfiguration(keyCase = KeyCase.LOWER, keySplitString = ".", key = "%3$s")
public interface BindingExceptionMessages {
  ModabiProperties modabi();

  String bindingObjects(Collection<? extends Object> bindingObjectStack);

  String bindingNodes(Collection<? extends BindingPoint<?>> bindingNodeStack);

  String noModelFound(QualifiedName modelName);

  String noModelFound(
      QualifiedName modelName,
      Collection<? extends Model<?>> candidates,
      TypeToken<?> type);

  String mustHaveData(QualifiedName node);

  String mustNotHaveData(BindingPoint<?> bindingPoint);

  default String mustHaveDataWithinRange(Child<?> node, Interval<Integer> range) {
    return mustHaveDataWithinRange(node.name(), Interval.compose(range));
  }

  String mustHaveDataWithinRange(String name, String compose);

  String noProviderFound(TypeToken<?> type);

  <T> String mustBeOrdered(Child<T> node, T lastItem, Class<? extends Comparator<?>> order);

  String cannotBindRemainingData(List<String> dataSource);

  String mustSupplyAttemptItems();

  String unexpectedProblemProcessing(Object data, Model<?> model);

  String unexpectedElement(QualifiedName element);

  String inverseCondition(String localizedMessage);

  String validationFailed(Child<?> bindingPoint, Expression expression);
}
