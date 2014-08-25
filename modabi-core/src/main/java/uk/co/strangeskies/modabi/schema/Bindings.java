package uk.co.strangeskies.modabi.schema;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import uk.co.strangeskies.gears.utilities.collection.HashSetMultiHashMap;
import uk.co.strangeskies.gears.utilities.collection.SetMultiMap;
import uk.co.strangeskies.modabi.model.AbstractModel;
import uk.co.strangeskies.modabi.model.Model;
import uk.co.strangeskies.modabi.model.Models;
import uk.co.strangeskies.modabi.model.nodes.ElementNode;

public class Bindings {
	public final Models models;
	public final SetMultiMap<AbstractModel<?, ?, ?>, Object> bindings;

	public Bindings() {
		models = new Models();
		bindings = new HashSetMultiHashMap<>();
	}

	public <T> void add(ElementNode<?> element, T data) {
		models.addAll(element.source().baseModel());
		bindings.addToAll(element.source().baseModel(), data);
	}

	public <T> void add(Model<T> model, T data) {
		models.add(model);
		bindings.add(model, data);
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
		Set<Object> all = bindings.get(model);

		if (all == null)
			all = new HashSet<>();

		all.addAll(Collections.unmodifiableSet(models.getDerivedModels(model)
				.stream().map(m -> bindings.get(m))
				.reduce(Collections.emptySet(), (s, t) -> {
					Set<Object> set = new HashSet<>();
					set.addAll(s);
					set.addAll(t);
					return set;
				})));

		return (Set<T>) all;
	}

	@Override
	public String toString() {
		return bindings.toString();
	}
}
