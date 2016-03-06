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
import java.util.function.Consumer;

import uk.co.strangeskies.modabi.schema.AbstractComplexNode;
import uk.co.strangeskies.modabi.schema.BindingNode;
import uk.co.strangeskies.modabi.schema.ComplexNode;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.utilities.Observable;
import uk.co.strangeskies.utilities.collection.MultiHashMap;
import uk.co.strangeskies.utilities.collection.MultiMap;

public class Bindings {
	public final MultiMap<Model.Effective<?>, BindingNode<?, ?, ?>, Set<BindingNode<?, ?, ?>>> boundNodes;
	public final MultiMap<BindingNode<?, ?, ?>, Object, Set<Object>> boundObjects;

	private final MultiMap<Model.Effective<?>, Consumer<?>, Collection<Consumer<?>>> listeners;

	public Bindings() {
		boundNodes = new MultiHashMap<>(HashSet::new);
		boundObjects = new MultiHashMap<>(HashSet::new);

		listeners = new MultiHashMap<>(HashSet::new);
	}

	public synchronized <T> void add(ComplexNode<T> element, T data) {
		element = element.source();

		synchronized (listeners) {
			boundNodes.addToAll(element.effective().baseModel(), element);
			boundObjects.add(element, data);

			fire(element.effective(), data);
		}
	}

	public synchronized <T> void add(Model<T> model, T data) {
		model = model.source();

		synchronized (listeners) {
			if (boundNodes.add(model.effective(), model)) {
				boundNodes.addToAll(model.effective().baseModel(), model);
			}
			boundObjects.add(model, data);

			fire(model.effective(), data);
		}
	}

	@SuppressWarnings("unchecked")
	private <T> void fire(AbstractComplexNode.Effective<T, ?, ?> node, T data) {
		for (Model.Effective<?> model : listeners.keySet()) {
			if (model.equals(node) || node.baseModel().contains(model)) {
				for (Consumer<?> listener : listeners.get(model)) {
					((Consumer<? super T>) listener).accept(data);
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
		add(binding.getModel(), binding.getData());
	}

	@SuppressWarnings("unchecked")
	public synchronized <T> Set<T> get(Model<T> model) {
		model = model.effective();

		synchronized (listeners) {
			return boundNodes.getOrDefault(model, emptySet()).stream()
					.flatMap(b -> boundObjects.getOrDefault(b, emptySet()).stream()).map(t -> (T) t).collect(toSet());
		}
	}

	public synchronized <T> Observable<T> changes(Model<T> model) {
		Model.Effective<T> effectiveModel = model.effective();

		return new Observable<T>() {
			@Override
			public boolean addObserver(Consumer<? super T> observer) {
				synchronized (Bindings.this) {
					return listeners.add(effectiveModel, observer);
				}
			}

			@Override
			public boolean removeObserver(Consumer<? super T> observer) {
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
