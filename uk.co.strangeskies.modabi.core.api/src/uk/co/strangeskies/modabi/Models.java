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
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.utilities.collection.MultiHashMap;
import uk.co.strangeskies.utilities.collection.MultiMap;

public class Models extends QualifiedNamedSet<Models, Model<?>> {
	private final MultiMap<QualifiedName, Model<?>, LinkedHashSet<Model<?>>> derivedModels;
	private final MultiMap<Type, Model<?>, LinkedHashSet<Model<?>>> classModels;

	public Models() {
		this(null);
	}

	protected Models(Models parent) {
		super(Model::getName, parent);
		derivedModels = new MultiHashMap<>(LinkedHashSet::new);
		classModels = new MultiHashMap<>(LinkedHashSet::new);
	}

	@Override
	public boolean add(Model<?> element) {
		boolean added = super.add(element.source());

		if (added)
			mapModel(element);

		return added;
	}

	private void mapModel(Model<?> model) {
		model = model.source();

		derivedModels.addToAll(model.effective().baseModel().stream().map(Model::getName).collect(Collectors.toSet()),
				model);

		if (!model.effective().isAbstract())
			classModels.add(model.effective().getDataType().getType(), model);
	}

	@SuppressWarnings("unchecked")
	public <T> List<Model<? extends T>> getDerivedModels(Model<T> model) {
		/*
		 * This extra cast is needed by javac but not JDT... Is it valid without?
		 */
		LinkedHashSet<Model<?>> subModelList = derivedModels.get(model.effective().getName());
		return subModelList == null ? new ArrayList<>()
				: subModelList.stream().map(m -> (Model<? extends T>) m).collect(Collectors.toCollection(ArrayList::new));
	}

	@SuppressWarnings("unchecked")
	public <T> List<Model<T>> getModelsWithClass(TypeToken<T> dataClass) {
		Set<Model<?>> models = classModels.get(dataClass.getType());
		return models == null ? Collections.emptyList()
				: models.stream().map(m -> (Model<T>) m).collect(Collectors.toList());
	}

	@SuppressWarnings("unchecked")
	public <T> List<Model<? extends T>> getModelsWithBase(Collection<? extends Model<? super T>> baseModel) {
		Iterator<? extends Model<?>> modelIterator = baseModel.iterator();

		List<? extends Model<?>> subModels = new ArrayList<>(getDerivedModels(modelIterator.next()));
		while (modelIterator.hasNext())
			subModels.retainAll(getDerivedModels(modelIterator.next()));

		modelIterator = subModels.iterator();
		while (modelIterator.hasNext())
			if (modelIterator.next().effective().isAbstract())
				modelIterator.remove();

		return (List<Model<? extends T>>) subModels;
	}

	@Override
	public Models copy() {
		Models copy = new Models();
		copy.addAll(this);
		return copy;
	}

	@Override
	public Models deriveChildScope() {
		return new Models(this);
	}
}
