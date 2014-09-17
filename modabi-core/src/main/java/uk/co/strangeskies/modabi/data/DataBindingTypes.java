package uk.co.strangeskies.modabi.data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.set.ListOrderedSet;

import uk.co.strangeskies.modabi.namespace.QualifiedNamedSet;
import uk.co.strangeskies.modabi.schema.model.nodes.DataNode;
import uk.co.strangeskies.utilities.collection.MultiHashMap;
import uk.co.strangeskies.utilities.collection.MultiMap;

public class DataBindingTypes extends QualifiedNamedSet<DataBindingType<?>> {
	private final MultiMap<DataBindingType<?>, DataBindingType<?>, ListOrderedSet<DataBindingType<?>>> derivedTypes;

	public DataBindingTypes() {
		super(DataBindingType::getName);
		derivedTypes = new MultiHashMap<>(() -> new ListOrderedSet<>());
	}

	@Override
	public boolean add(DataBindingType<?> element) {
		boolean added = super.add(element.source());

		if (added)
			mapType(element);

		return added;
	}

	private void mapType(DataBindingType<?> type) {
		derivedTypes.add(type.effective().baseType(), type.source());
	}

	@SuppressWarnings("unchecked")
	public <T> List<DataBindingType<? extends T>> getDerivedTypes(
			DataBindingType<T> type) {
		/*
		 * TODO This extra cast is needed by javac but not JDT... Is it valid
		 * without?
		 */
		ListOrderedSet<DataBindingType<?>> subTypeList = derivedTypes.get(type
				.source());
		return subTypeList == null ? new ArrayList<>()
				: new ArrayList<DataBindingType<? extends T>>(subTypeList.stream()
						.map(m -> (DataBindingType<? extends T>) m)
						.collect(Collectors.toList()));
	}

	public <T> List<DataBindingType<? extends T>> getMatchingTypes(
			DataNode<T> node, Class<?> dataClass) {
		List<DataBindingType<? extends T>> subTypes = getDerivedTypes(node
				.effective().type());

		subTypes = subTypes
				.stream()
				.filter(
						m -> !m.effective().isAbstract()
								&& m.effective().getDataClass().isAssignableFrom(dataClass))
				.collect(Collectors.toList());

		return subTypes;

		// ClassUtils.getAllSuperclasses(dataClass); // TODO with no baseModel
	}
}
