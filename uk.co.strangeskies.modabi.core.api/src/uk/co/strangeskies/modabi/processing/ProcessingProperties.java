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
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import uk.co.strangeskies.mathematics.Range;
import uk.co.strangeskies.modabi.ModabiProperties;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.schema.BindingChildNode;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.SchemaNode;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.text.properties.Localized;
import uk.co.strangeskies.text.properties.Properties;

public interface ProcessingProperties extends Properties<ProcessingProperties> {
	ModabiProperties modabiException();

	Localized<String> bindingObjects(Collection<? extends Object> bindingObjectStack);

	Localized<String> bindingNodes(Collection<? extends SchemaNode<?>> bindingNodeStack);

	Localized<String> noModelFound(QualifiedName modelName);

	Localized<String> noModelFound(QualifiedName modelName, Collection<? extends Model<?>> candidates, TypeToken<?> type);

	Localized<String> mustHaveChildren(QualifiedName name, InputBindingStrategy strategy);

	Localized<String> cannotInvoke(Executable inputMethod, TypeToken<?> targetType, SchemaNode<?> node,
			List<?> parameters);

	Localized<String> mustHaveData(QualifiedName node);

	Localized<String> noFormatFound();

	Localized<String> noFormatFoundFor(String id);

	Localized<String> noProviderFound(TypeToken<?> type);

	default Localized<String> mustHaveDataWithinRange(BindingChildNode<?, ?> node) {
		return mustHaveDataWithinRange(node.name(), Range.compose(node.occurrences()));
	}

	Localized<String> mustHaveDataWithinRange(QualifiedName name, String compose);

	default Localized<String> cannotBindRemainingData(DataSource dataSource) {
		return cannotBindRemainingData(dataSource.stream().map(Objects::toString).collect(Collectors.toList()));
	}

	Localized<String> cannotBindRemainingData(List<String> dataSource);

	Localized<String> mustSupplyAttemptItems();

	Localized<String> unexpectedProblemProcessing(Object data, Model<?> model);
}
