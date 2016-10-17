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
package uk.co.strangeskies.modabi.processing;

import java.lang.reflect.Executable;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import uk.co.strangeskies.mathematics.Range;
import uk.co.strangeskies.modabi.ModabiProperties;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.schema.BindingPoint;
import uk.co.strangeskies.modabi.schema.ChildBindingPoint;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.SchemaNode;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.text.properties.Localized;
import uk.co.strangeskies.text.properties.Properties;
import uk.co.strangeskies.text.properties.PropertyConfiguration;
import uk.co.strangeskies.text.properties.PropertyConfiguration.KeyCase;

@PropertyConfiguration(keyCase = KeyCase.LOWER, keySplitString = ".", key = "%3$s")
public interface ProcessingProperties extends Properties<ProcessingProperties> {
	ModabiProperties modabi();

	Localized<String> bindingObjects(Collection<? extends Object> bindingObjectStack);

	Localized<String> bindingNodes(Collection<? extends BindingPoint<?>> bindingNodeStack);

	Localized<String> noModelFound(QualifiedName modelName);

	Localized<String> noModelFound(QualifiedName modelName, Collection<? extends Model<?>> candidates, TypeToken<?> type);

	Localized<String> cannotInvoke(Executable inputMethod, TypeToken<?> targetType, SchemaNode node, List<?> parameters);

	Localized<String> mustHaveData(QualifiedName node);

	Localized<String> mustNotHaveData(QualifiedName node);

	Localized<String> noFormatFound();

	Localized<String> noFormatFoundFor(String id);

	Localized<String> noProviderFound(TypeToken<?> type);

	<T> Localized<String> mustBeOrdered(ChildBindingPoint<T> node, List<? extends T> data,
			Class<? extends Comparator<?>> order);

	default Localized<String> mustHaveDataWithinRange(ChildBindingPoint<?> node, Range<Integer> range) {
		return mustHaveDataWithinRange(node.name(), Range.compose(range));
	}

	Localized<String> mustHaveDataWithinRange(QualifiedName name, String compose);

	default Localized<String> cannotBindRemainingData(DataSource dataSource) {
		return cannotBindRemainingData(dataSource.stream().map(Objects::toString).collect(Collectors.toList()));
	}

	Localized<String> cannotBindRemainingData(List<String> dataSource);

	Localized<String> mustSupplyAttemptItems();

	Localized<String> unexpectedProblemProcessing(Object data, BindingPoint<?> model);

	Localized<String> unexpectedElement(QualifiedName element);

	Localized<String> inverseCondition(Localized<String> localizedMessage);
}
