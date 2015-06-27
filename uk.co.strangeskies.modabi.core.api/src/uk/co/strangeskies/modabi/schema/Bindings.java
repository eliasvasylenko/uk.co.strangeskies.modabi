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
package uk.co.strangeskies.modabi.schema;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.management.Models;
import uk.co.strangeskies.modabi.schema.node.ComplexNode;
import uk.co.strangeskies.modabi.schema.node.Model;
import uk.co.strangeskies.utilities.collection.multimap.MultiHashMap;
import uk.co.strangeskies.utilities.collection.multimap.MultiMap;

public class Bindings {
	public final Models models;
	public final MultiMap<QualifiedName, Object, Set<Object>> bindings;

	public Bindings() {
		models = new Models();
		bindings = new MultiHashMap<>(HashSet::new);
	}

	public <T> void add(ComplexNode<?> element, T data) {
		models.addAll(element.source().baseModel());
		bindings.addToAll(
				element.source().baseModel().stream().map(n -> n.effective().getName())
						.collect(Collectors.toSet()), data);
	}

	public <T> void add(Model<T> model, T data) {
		models.add(model);
		bindings.add(model.effective().getName(), data);
	}

	public void add(Binding<?>... bindings) {
		add(Arrays.asList(bindings));
	}

	public void add(Collection<? extends Binding<?>> bindings) {
		for (Binding<?> binding : bindings)
			addGeneric(binding);
	}

	private <T> void addGeneric(Binding<T> binding) {
		add(binding.getModel(), binding.getData());
	}

	@SuppressWarnings("unchecked")
	public <T> Set<T> get(Model<T> model) {
		Set<Object> all = bindings.get(model.effective().getName());

		if (all == null)
			all = new HashSet<>();

		all.addAll(models.getDerivedModels(model).stream()
				.map(m -> bindings.get(m.effective().getName()))
				.reduce(Collections.emptySet(), (s, t) -> {
					Set<Object> set = new HashSet<>();
					set.addAll(s);
					set.addAll(t);
					return set;
				}));

		return (Set<T>) all;
	}

	@Override
	public String toString() {
		return bindings.toString();
	}
}
