package uk.co.strangeskies.modabi.schema;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import uk.co.strangeskies.modabi.schema.node.AbstractComplexNode;
import uk.co.strangeskies.modabi.schema.node.ComplexNode;
import uk.co.strangeskies.modabi.schema.node.model.Model;
import uk.co.strangeskies.modabi.schema.node.model.Models;
import uk.co.strangeskies.utilities.collection.multimap.MultiHashMap;
import uk.co.strangeskies.utilities.collection.multimap.MultiMap;

public class Bindings {
	public final Models models;
	public final MultiMap<AbstractComplexNode<?, ?, ?>, Object, Set<Object>> bindings;

	public Bindings() {
		models = new Models();
		bindings = new MultiHashMap<>(HashSet::new);
	}

	public <T> void add(ComplexNode<?> element, T data) {
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

		all.addAll(models.getDerivedModels(model).stream()
				.map(m -> bindings.get(m)).reduce(Collections.emptySet(), (s, t) -> {
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