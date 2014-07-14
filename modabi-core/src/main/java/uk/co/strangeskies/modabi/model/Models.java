package uk.co.strangeskies.modabi.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.set.ListOrderedSet;

import uk.co.strangeskies.gears.utilities.collection.ArrayListMultiHashMap;
import uk.co.strangeskies.gears.utilities.collection.ListMultiMap;
import uk.co.strangeskies.gears.utilities.collection.MultiHashMap;
import uk.co.strangeskies.gears.utilities.collection.MultiMap;
import uk.co.strangeskies.modabi.namespace.NamedSet;
import uk.co.strangeskies.modabi.namespace.Namespace;

public class Models extends NamedSet<Model<?>> {
	private final MultiMap<Model<?>, Model<?>, ListOrderedSet<Model<?>>> derivedModels;
	private final ListMultiMap<Class<?>, Model<?>> classes;

	public Models(Namespace namespace) {
		super(namespace, t -> t.getId());
		derivedModels = new MultiHashMap<>(() -> new ListOrderedSet<>());
		classes = new ArrayListMultiHashMap<>();
	}

	@Override
	public boolean add(Model<?> element) {
		boolean added = super.add(element);

		if (added)
			mapModel(element);

		return added;
	}

	@Override
	public boolean add(Model<?> model, Namespace namespace) {
		boolean added = super.add(model, namespace);

		if (added)
			mapModel(model);

		return added;
	}

	private void mapModel(Model<?> model) {
		ListOrderedSet<Model<?>> baseModels = new ListOrderedSet<>();
		baseModels.addAll(model.effectiveModel().baseModel());
		for (int i = 0; i < baseModels.size(); i++) {
			Model<?> baseModel = baseModels.get(i);
			derivedModels.add(baseModel, model);
			baseModels.addAll(baseModel.effectiveModel().baseModel());
		}

		classes.add(model.effectiveModel().getDataClass(), model);
	}

	@SuppressWarnings("unchecked")
	public <T> List<Model<? extends T>> getDerivedModels(AbstractModel<T> model) {
		/*
		 * TODO This extra cast is needed by javac but not JDT... Is it valid
		 * without?
		 */
		Object derivedModelListObject = derivedModels.get(model);
		ListOrderedSet<? extends Model<? extends T>> subModelList = (ListOrderedSet<? extends Model<? extends T>>) derivedModelListObject;
		return subModelList == null ? new ArrayList<>() : new ArrayList<>(
				subModelList);
	}

	@SuppressWarnings("unchecked")
	public <T> List<Model<? extends T>> getMatchingModels(
			AbstractModel<T> element, Class<?> dataClass) {
		Iterator<? extends Model<?>> baseModelIterator = element.baseModel()
				.iterator();

		List<? extends Model<?>> subModels = new ArrayList<>(
				getDerivedModels(baseModelIterator.next()));
		while (baseModelIterator.hasNext())
			subModels.retainAll(getDerivedModels(baseModelIterator.next()));

		subModels = subModels
				.stream()
				.filter(
						m -> (m.effectiveModel().isAbstract() == null || !m
								.effectiveModel().isAbstract())
								&& m.effectiveModel().getDataClass()
										.isAssignableFrom(dataClass)).collect(Collectors.toList());

		return (List<Model<? extends T>>) subModels;

		// ClassUtils.getAllSuperclasses(dataClass); // TODO with no baseModel
	}
}
