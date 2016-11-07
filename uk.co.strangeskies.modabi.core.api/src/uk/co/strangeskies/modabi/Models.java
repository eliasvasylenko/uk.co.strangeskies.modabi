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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.reflection.token.TypeToken;
import uk.co.strangeskies.utilities.collection.MultiHashMap;
import uk.co.strangeskies.utilities.collection.MultiMap;

public class Models extends NamedSet<Models, QualifiedName, Model<?>> {
	private final MultiMap<QualifiedName, Model<?>, LinkedHashSet<Model<?>>> derivedModels;
	private final MultiMap<Type, Model<?>, LinkedHashSet<Model<?>>> classModels;

	public Models() {
		this(null);
	}

	protected Models(Models parent) {
		super(Model::name, parent);
		derivedModels = new MultiHashMap<>(LinkedHashSet::new);
		classModels = new MultiHashMap<>(LinkedHashSet::new);
	}

	@Override
	public boolean add(Model<?> element) {
		synchronized (getComponent()) {
			boolean added = super.add(element);

			if (added)
				mapModel(element);

			return added;
		}
	}

	private void mapModel(Model<?> model) {
		List<Model<?>> models = new ArrayList<>();
		models.add(model);
		models.addAll(model.baseModel());

		derivedModels.addToAll(models.stream().map(Model::name).collect(Collectors.toSet()), model);

		if (model.concrete())
			classModels.add(model.dataType().getType(), model);
	}

	@SuppressWarnings("unchecked")
	public <T> List<Model<? extends T>> getDerivedModels(Model<T> model) {
		synchronized (getMutex()) {
			LinkedHashSet<Model<?>> subModelList = derivedModels.get(model.name());

			List<Model<? extends T>> derivedModelList = subModelList == null ? new ArrayList<>()
					: subModelList.stream().map(m -> (Model<? extends T>) m).collect(Collectors.toCollection(ArrayList::new));

			getParentScope().ifPresent(p -> derivedModelList.addAll(p.getDerivedModels(model)));

			return derivedModelList;
		}
	}

	public <T> Model<T> get(QualifiedName name, TypeToken<T> dataType) {
		@SuppressWarnings("unchecked")
		Model<T> model = (Model<T>) get(name);

		checkType(model, dataType);

		return model;
	}

	private <T> void checkType(Model<T> model, TypeToken<T> dataType) {
		if (model != null && !model.dataType().isAssignableFrom(dataType)) {
			throw new ModabiException(t -> t.noModelFoundForType(model.name(), dataType.getType()));
		}
	}

	public <T> Model<T> waitForGet(QualifiedName name, TypeToken<T> dataType) throws InterruptedException {
		return waitForGet(name, dataType, () -> {});
	}

	public <T> Model<T> waitForGet(QualifiedName name, TypeToken<T> dataType, Runnable onPresent)
			throws InterruptedException {
		return waitForGet(name, dataType, onPresent, -1);
	}

	public <T> Model<T> waitForGet(QualifiedName name, TypeToken<T> dataType, int timeoutMilliseconds)
			throws InterruptedException {
		return waitForGet(name, dataType, () -> {}, timeoutMilliseconds);
	}

	public <T> Model<T> waitForGet(QualifiedName name, TypeToken<T> dataType, Runnable onPresent, int timeoutMilliseconds)
			throws InterruptedException {
		@SuppressWarnings("unchecked")
		Model<T> model = (Model<T>) waitForGet(name);

		checkType(model, dataType);

		return model;
	}

	@SuppressWarnings("unchecked")
	public <T> List<Model<T>> getModelsWithType(TypeToken<T> dataType) {
		synchronized (getMutex()) {
			Set<Model<?>> models = classModels.get(dataType.getType());

			List<Model<T>> modelsWithType = models == null ? Collections.emptyList()
					: models.stream().map(m -> (Model<T>) m).collect(Collectors.toList());

			getParentScope().ifPresent(p -> modelsWithType.addAll(p.getModelsWithType(dataType)));

			return modelsWithType;
		}
	}

	@SuppressWarnings("unchecked")
	public <T> List<Model<? extends T>> getModelsWithBase(Collection<? extends Model<? super T>> baseModel) {
		synchronized (getMutex()) {
			Iterator<? extends Model<? extends T>> modelIterator = (Iterator<? extends Model<? extends T>>) baseModel
					.iterator();

			List<Model<? extends T>> subModels = new ArrayList<Model<? extends T>>(getDerivedModels(modelIterator.next()));

			modelIterator = subModels.iterator();
			while (modelIterator.hasNext())
				if (!modelIterator.next().concrete())
					modelIterator.remove();

			while (modelIterator.hasNext())
				subModels.retainAll(getDerivedModels(modelIterator.next()));

			getParentScope().ifPresent(p -> subModels.addAll(p.getModelsWithBase(baseModel)));

			return subModels;
		}
	}

	@Override
	public Models copy() {
		synchronized (getMutex()) {
			Models copy = new Models();
			copy.addAll(this);
			return copy;
		}
	}

	@Override
	public Models nestChildScope() {
		return new Models(this);
	}
}
