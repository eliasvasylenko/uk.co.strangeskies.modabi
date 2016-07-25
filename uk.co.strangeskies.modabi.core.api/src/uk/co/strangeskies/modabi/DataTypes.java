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
package uk.co.strangeskies.modabi;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.schema.DataNode;
import uk.co.strangeskies.modabi.schema.DataType;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.utilities.collection.MultiHashMap;
import uk.co.strangeskies.utilities.collection.MultiMap;

public class DataTypes extends NamedSet<DataTypes, QualifiedName, DataType<?>> {
	private final MultiMap<QualifiedName, DataType<?>, LinkedHashSet<DataType<?>>> derivedTypes;

	public DataTypes() {
		this(null);
	}

	public DataTypes(DataTypes parent) {
		super(DataType::name, parent);
		derivedTypes = new MultiHashMap<>(() -> new LinkedHashSet<>());
	}

	@Override
	public boolean add(DataType<?> element) {
		synchronized (getMutex()) {
			boolean added = super.add(element);

			if (added)
				mapType(element);

			return added;
		}
	}

	public <T> DataType<T> get(QualifiedName name, TypeToken<T> dataType) {
		@SuppressWarnings("unchecked")
		DataType<T> model = (DataType<T>) get(name);

		checkType(model, dataType);

		return model;
	}

	private <T> void checkType(DataType<T> type, TypeToken<T> dataType) {
		if (type != null && !type.dataType().isAssignableFrom(dataType)) {
			throw new ModabiException(t -> t.noTypeFoundForType(type.name(), dataType.getType()));
		}
	}

	public <T> DataType<T> waitForGet(QualifiedName name, TypeToken<T> dataType) throws InterruptedException {
		return waitForGet(name, dataType, () -> {});
	}

	public <T> DataType<T> waitForGet(QualifiedName name, TypeToken<T> dataType, Runnable onPresent)
			throws InterruptedException {
		return waitForGet(name, dataType, onPresent, -1);
	}

	public <T> DataType<T> waitForGet(QualifiedName name, TypeToken<T> dataType, int timeoutMilliseconds)
			throws InterruptedException {
		return waitForGet(name, dataType, () -> {}, timeoutMilliseconds);
	}

	public <T> DataType<T> waitForGet(QualifiedName name, TypeToken<T> dataType, Runnable onPresent,
			int timeoutMilliseconds) throws InterruptedException {
		@SuppressWarnings("unchecked")
		DataType<T> model = (DataType<T>) waitForGet(name);

		checkType(model, dataType);

		return model;
	}

	private void mapType(DataType<?> type) {
		if (type.baseType() != null)
			derivedTypes.add(type.baseType().name(), type);
	}

	@SuppressWarnings("unchecked")
	public <T> List<DataType<? extends T>> getDerivedTypes(DataType<T> type) {
		synchronized (getMutex()) {
			LinkedHashSet<DataType<?>> subTypeList = derivedTypes.get(type.name());

			List<DataType<? extends T>> derivedTypes = subTypeList == null ? new ArrayList<>()
					: new ArrayList<>(subTypeList.stream().map(m -> (DataType<? extends T>) m).collect(Collectors.toList()));

			getParentScope().ifPresent(p -> derivedTypes.addAll(p.getDerivedTypes(type)));

			return derivedTypes;
		}
	}

	public <T> List<DataType<?>> getTypesWithBase(DataNode<T> node) {
		synchronized (getMutex()) {
			List<DataType<?>> subTypes =

					getDerivedTypes(node.type())

							.stream().filter(m -> m.abstractness().isAtMost(Abstractness.UNINFERRED)).collect(Collectors.toList());

			getParentScope().ifPresent(p -> subTypes.addAll(p.getTypesWithBase(node)));

			return subTypes;
		}
	}

	@Override
	public DataTypes copy() {
		synchronized (getMutex()) {
			DataTypes copy = new DataTypes();
			copy.addAll(this);
			return copy;
		}
	}

	@Override
	public DataTypes nestChildScope() {
		return new DataTypes(this);
	}
}
