/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
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
package uk.co.strangeskies.modabi;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.schema.DataType;
import uk.co.strangeskies.modabi.schema.DataNode;
import uk.co.strangeskies.utilities.collection.MultiHashMap;
import uk.co.strangeskies.utilities.collection.MultiMap;

public class DataTypes extends QualifiedNamedSet<DataType<?>> {
	private final MultiMap<QualifiedName, DataType<?>, LinkedHashSet<DataType<?>>> derivedTypes;

	public DataTypes() {
		super(DataType::getName);
		derivedTypes = new MultiHashMap<>(() -> new LinkedHashSet<>());
	}

	@Override
	public boolean add(DataType<?> element) {
		boolean added = super.add(element.source());

		if (added)
			mapType(element);

		return added;
	}

	private void mapType(DataType<?> type) {
		if (type.effective().baseType() != null)
			derivedTypes.add(type.effective().baseType().getName(), type.source());
	}

	@SuppressWarnings("unchecked")
	public <T> List<DataType<? extends T>> getDerivedTypes(DataType<T> type) {
		/*
		 * This extra cast is needed by javac but not JDT... Is it valid without?
		 */
		LinkedHashSet<DataType<?>> subTypeList = derivedTypes
				.get(type.effective().getName());
		return subTypeList == null ? new ArrayList<>()
				: new ArrayList<DataType<? extends T>>(subTypeList.stream()
						.map(m -> (DataType<? extends T>) m).collect(Collectors.toList()));
	}

	public <T> List<DataType<? extends T>> getTypesWithBase(DataNode<T> node) {
		List<DataType<? extends T>> subTypes = getDerivedTypes(
				node.effective().type());

		subTypes = subTypes.stream().filter(m -> !m.effective().isAbstract())
				.collect(Collectors.toList());

		return subTypes;
	}
}
