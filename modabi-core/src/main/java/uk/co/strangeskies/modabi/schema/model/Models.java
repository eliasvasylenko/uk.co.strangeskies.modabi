package uk.co.strangeskies.modabi.schema.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.set.ListOrderedSet;

import uk.co.strangeskies.modabi.namespace.QualifiedNamedSet;
import uk.co.strangeskies.utilities.collection.MultiHashMap;
import uk.co.strangeskies.utilities.collection.MultiMap;

public class Models extends QualifiedNamedSet<Model<?>> {
	private final MultiMap<Model<?>, Model<?>, ListOrderedSet<Model<?>>> derivedModels;

	public Models() {
		super(t -> t.getName());
		derivedModels = new MultiHashMap<>(() -> new ListOrderedSet<>());
	}

	@Override
	public boolean add(Model<?> element) {
		boolean added = super.add(element.source());

		if (added)
			mapModel(element);

		return added;
	}

	private void mapModel(Model<?> model) {
		derivedModels.addToAll(
				model.effective().baseModel().stream().map(Model::source)
						.collect(Collectors.toSet()), model.source());
	}

	@SuppressWarnings("unchecked")
	public <T> List<Model<? extends T>> getDerivedModels(Model<T> model) {
		/*
		 * TODO This extra cast is needed by javac but not JDT... Is it valid
		 * without?
		 */
		ListOrderedSet<Model<?>> subModelList = derivedModels.get(model.source());
		return subModelList == null ? new ArrayList<>()
				: new ArrayList<Model<? extends T>>(subModelList.stream()
						.map(m -> (Model<? extends T>) m).collect(Collectors.toList()));
	}

	@SuppressWarnings("unchecked")
	public <T> List<Model<? extends T>> getMatchingModels(
			AbstractModel.Effective<T, ?, ?> element, Class<?> dataClass) {
		Iterator<? extends Model<?>> baseModelIterator = element.baseModel()
				.iterator();

		List<? extends Model<?>> subModels = new ArrayList<>(
				getDerivedModels(baseModelIterator.next()));
		while (baseModelIterator.hasNext())
			subModels.retainAll(getDerivedModels(baseModelIterator.next()));

		subModels = subModels
				.stream()
				.filter(
						m -> !m.effective().isAbstract()
								&& m.effective().getDataClass().isAssignableFrom(dataClass))
				.collect(Collectors.toList());

		return (List<Model<? extends T>>) subModels;

		// ClassUtils.getAllSuperclasses(dataClass); // TODO with no baseModel
	}
}
