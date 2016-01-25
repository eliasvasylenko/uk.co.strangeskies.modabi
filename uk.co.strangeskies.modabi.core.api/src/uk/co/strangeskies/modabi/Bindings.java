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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.schema.BindingNode;
import uk.co.strangeskies.modabi.schema.ComplexNode;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.utilities.collection.MultiHashMap;
import uk.co.strangeskies.utilities.collection.MultiMap;

public class Bindings {
	public final MultiMap<Model.Effective<?>, BindingNode<?, ?, ?>, Set<BindingNode<?, ?, ?>>> boundNodes;
	public final MultiMap<BindingNode<?, ?, ?>, Object, Set<Object>> bindings;

	public Bindings() {
		boundNodes = new MultiHashMap<>(HashSet::new);
		bindings = new MultiHashMap<>(HashSet::new);
	}

	public <T> void add(ComplexNode<T> element, T data) {
		element = element.source();

		boundNodes.addToAll(element.effective().baseModel(), element);
		bindings.add(element, data);
	}

	public <T> void add(Model<T> model, T data) {
		model = model.source();

		if (boundNodes.add(model.effective(), model)) {
			boundNodes.addToAll(model.effective().baseModel(), model);
		}
		bindings.add(model, data);
	}

	public void add(Binding<?>... bindings) {
		add(Arrays.asList(bindings));
	}

	public void add(Collection<? extends Binding<?>> bindings) {
		for (Binding<?> binding : bindings)
			addCapture(binding);
	}

	private <T> void addCapture(Binding<T> binding) {
		add(binding.getModel(), binding.getData());
	}

	@SuppressWarnings("unchecked")
	public <T> Set<T> get(Model<T> model) {
		model = model.effective();

		return boundNodes.getOrDefault(model, Collections.emptySet()).stream()
				.flatMap(b -> bindings.getOrDefault(b, Collections.emptySet()).stream())
				.map(t -> (T) t).collect(Collectors.toSet());
	}

	@Override
	public String toString() {
		return bindings.toString();
	}
}
