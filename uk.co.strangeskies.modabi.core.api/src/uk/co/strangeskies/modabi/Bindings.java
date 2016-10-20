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

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import uk.co.strangeskies.modabi.schema.BindingPoint;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.utilities.Observable;
import uk.co.strangeskies.utilities.Observer;
import uk.co.strangeskies.utilities.collection.MultiHashMap;
import uk.co.strangeskies.utilities.collection.MultiMap;

public class Bindings {
	public final MultiMap<Model<?>, BindingPoint<?>, Set<BindingPoint<?>>> boundNodes;
	public final MultiMap<BindingPoint<?>, Object, Set<Object>> boundObjects;

	private final MultiMap<Model<?>, Observer<?>, Collection<Observer<?>>> listeners;

	public Bindings() {
		boundNodes = new MultiHashMap<>(HashSet::new);
		boundObjects = new MultiHashMap<>(HashSet::new);

		listeners = new MultiHashMap<>(HashSet::new);
	}

	public synchronized <T> void add(BindingPoint<T> element, T data) {
		BindingPoint<T> effectiveElement = element;

		boundNodes.addToAll(effectiveElement.baseModel(), effectiveElement);
		boundObjects.add(effectiveElement, data);

		fire(effectiveElement, data);
	}

	public synchronized <T> void add(Model<T> model, T data) {
		Model<T> effectiveModel = model;

		if (boundNodes.add(effectiveModel, effectiveModel)) {
			boundNodes.addToAll(effectiveModel.baseModel(), effectiveModel);
		}
		boundObjects.add(effectiveModel, data);

		fire(effectiveModel, data);
	}

	@SuppressWarnings("unchecked")
	private <T> void fire(BindingPoint<T> node, T data) {
		for (Model<?> model : listeners.keySet()) {
			if (model.equals(node) || node.baseModel().contains(model)) {
				for (Observer<?> listener : listeners.get(model)) {
					((Observer<? super T>) listener).notify(data);
				}
			}
		}
	}

	public synchronized void add(Binding<?>... bindings) {
		add(Arrays.asList(bindings));
	}

	public synchronized void add(Collection<? extends Binding<?>> bindings) {
		for (Binding<?> binding : bindings)
			addCapture(binding);
	}

	private <T> void addCapture(Binding<T> binding) {
		add(binding.getNode(), binding.getData());
	}

	@SuppressWarnings("unchecked")
	public synchronized <T> Set<T> getModelBindings(Model<T> model) {
		return boundNodes
				.getOrDefault(model, emptySet())
				.stream()
				.flatMap(b -> boundObjects.getOrDefault(b, emptySet()).stream())
				.map(t -> (T) t)
				.collect(toSet());
	}

	@SuppressWarnings("unchecked")
	public synchronized <T> Set<T> getNodeBindings(BindingPoint<T> node) {
		return boundObjects.getOrDefault(node, emptySet()).stream().map(t -> (T) t).collect(toSet());
	}

	public synchronized <T> Observable<T> changes(Model<T> model) {
		Model<T> effectiveModel = model;

		return new Observable<T>() {
			@Override
			public boolean addObserver(Observer<? super T> observer) {
				synchronized (Bindings.this) {
					return listeners.add(effectiveModel, observer);
				}
			}

			@Override
			public boolean removeObserver(Observer<? super T> observer) {
				synchronized (Bindings.this) {
					return listeners.add(effectiveModel, observer);
				}
			}
		};
	}

	@Override
	public String toString() {
		return boundObjects.toString();
	}
}
