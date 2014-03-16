package uk.co.strangeskies.modabi.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections4.set.ListOrderedSet;

import uk.co.strangeskies.gears.utilities.collection.ArrayListMultiHashMap;
import uk.co.strangeskies.gears.utilities.collection.ListMultiMap;
import uk.co.strangeskies.gears.utilities.collection.MultiHashMap;
import uk.co.strangeskies.gears.utilities.collection.MultiMap;
import uk.co.strangeskies.modabi.namespace.NamedSet;
import uk.co.strangeskies.modabi.namespace.Namespace;

public class Models extends NamedSet<Model<?>> {
	private final MultiMap<Model<?>, Model<?>, ListOrderedSet<Model<?>>> subModels;
	private final ListMultiMap<Class<?>, Model<?>> classes;

	public Models(Namespace namespace) {
		super(namespace, new Function<Model<?>, String>() {
			@Override
			public String apply(Model<?> t) {
				return t.getId();
			}
		});
		subModels = new MultiHashMap<>(() -> new ListOrderedSet<>());
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
		baseModels.addAll(model.effectiveModel().getBaseModel());
		for (int i = 0; i < baseModels.size(); i++) {
			Model<?> baseModel = baseModels.get(i);
			subModels.add(baseModel, model);
			baseModels.addAll(baseModel.effectiveModel().getBaseModel());
		}

		classes.add(model.effectiveModel().getDataClass(), model);
	}

	@SuppressWarnings("unchecked")
	public <T> List<Model<? extends T>> getSubModels(AbstractModel<T> model) {
		ListOrderedSet<? extends Model<? extends T>> subModelList = (ListOrderedSet<? extends Model<? extends T>>) subModels
				.get(model);
		return subModelList == null ? new ArrayList<>() : new ArrayList<>(
				subModelList);
	}

	@SuppressWarnings("unchecked")
	public <T> Model<? extends T> getMatchingModel(AbstractModel<T> element,
			Class<?> dataClass) {
		Iterator<? extends Model<?>> baseModelIterator = element.getBaseModel()
				.iterator();

		List<Model<?>> subModels = new ArrayList<>(
				getSubModels(baseModelIterator.next()));
		while (baseModelIterator.hasNext())
			subModels.retainAll(getSubModels(baseModelIterator.next()));

		subModels = subModels
				.stream()
				.filter(
						m -> (m.effectiveModel().isAbstract() == null || !m
								.effectiveModel().isAbstract())
								&& m.effectiveModel().getDataClass()
										.isAssignableFrom(dataClass)).collect(Collectors.toList());

		return (Model<? extends T>) subModels.get(subModels.size() - 1);

		// ClassUtils.getAllSuperclasses(dataClass); // TODO with no baseModel
	}
}
