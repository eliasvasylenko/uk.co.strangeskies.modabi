package uk.co.strangeskies.modabi.schema.node.type;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.namespace.QualifiedNamedSet;
import uk.co.strangeskies.modabi.schema.node.DataNode;
import uk.co.strangeskies.utilities.collection.multimap.MultiHashMap;
import uk.co.strangeskies.utilities.collection.multimap.MultiMap;

public class DataBindingTypes extends QualifiedNamedSet<DataBindingType<?>> {
	private final MultiMap<DataBindingType<?>, DataBindingType<?>, LinkedHashSet<DataBindingType<?>>> derivedTypes;

	public DataBindingTypes() {
		super(DataBindingType::getName);
		derivedTypes = new MultiHashMap<>(() -> new LinkedHashSet<>());
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
		LinkedHashSet<DataBindingType<?>> subTypeList = derivedTypes.get(type
				.source());
		return subTypeList == null ? new ArrayList<>()
				: new ArrayList<DataBindingType<? extends T>>(subTypeList.stream()
						.map(m -> (DataBindingType<? extends T>) m)
						.collect(Collectors.toList()));
	}

	public <T> List<DataBindingType<? extends T>> getTypesWithBase(
			DataNode<T> node) {
		List<DataBindingType<? extends T>> subTypes = getDerivedTypes(node
				.effective().type());

		subTypes = subTypes.stream().filter(m -> !m.effective().isAbstract())
				.collect(Collectors.toList());

		return subTypes;
	}
}
