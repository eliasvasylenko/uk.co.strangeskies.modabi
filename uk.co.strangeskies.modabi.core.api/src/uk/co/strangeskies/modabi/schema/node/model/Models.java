package uk.co.strangeskies.modabi.schema.node.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.namespace.QualifiedNamedSet;
import uk.co.strangeskies.reflection.TypeLiteral;
import uk.co.strangeskies.utilities.collection.multimap.MultiHashMap;
import uk.co.strangeskies.utilities.collection.multimap.MultiMap;

public class Models extends QualifiedNamedSet<Model<?>> {
	private final MultiMap<Model<?>, Model<?>, LinkedHashSet<Model<?>>> derivedModels;
	private final MultiMap<TypeLiteral<?>, Model<?>, Set<Model<?>>> classModels;

	public Models() {
		super(Model::getName);
		derivedModels = new MultiHashMap<>(() -> new LinkedHashSet<>());
		classModels = new MultiHashMap<>(HashSet::new);
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

		if (!model.effective().isAbstract())
			classModels.add(model.getDataType(), model);
	}

	@SuppressWarnings("unchecked")
	public <T> List<Model<? extends T>> getDerivedModels(Model<T> model) {
		/*
		 * TODO This extra cast is needed by javac but not JDT... Is it valid
		 * without?
		 */
		LinkedHashSet<Model<?>> subModelList = derivedModels.get(model.source());
		return subModelList == null ? new ArrayList<>() : subModelList.stream()
				.map(m -> (Model<? extends T>) m)
				.collect(Collectors.toCollection(ArrayList::new));
	}

	@SuppressWarnings("unchecked")
	public <T> List<Model<T>> getModelsWithClass(TypeLiteral<T> dataClass) {
		Set<Model<?>> models = classModels.get(dataClass);
		return models == null ? Collections.emptyList() : models.stream()
				.map(m -> (Model<T>) m).collect(Collectors.toList());
	}

	@SuppressWarnings("unchecked")
	public <T> List<Model<? extends T>> getModelsWithBase(
			Collection<? extends Model<? super T>> baseModel) {
		Iterator<? extends Model<? super T>> baseModelIterator = baseModel
				.iterator();

		List<? extends Model<?>> subModels = new ArrayList<>(
				getDerivedModels(baseModelIterator.next()));
		while (baseModelIterator.hasNext())
			subModels.retainAll(getDerivedModels(baseModelIterator.next()));

		subModels = subModels.stream().filter(m -> !m.effective().isAbstract())
				.collect(Collectors.toList());

		return (List<Model<? extends T>>) subModels;
	}
}
