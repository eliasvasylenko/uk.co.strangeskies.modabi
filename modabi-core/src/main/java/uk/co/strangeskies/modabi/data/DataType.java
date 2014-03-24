package uk.co.strangeskies.modabi.data;

import java.util.List;

import uk.co.strangeskies.modabi.model.nodes.PropertyNode;

public interface DataType<T> {
	String getName();

	Class<T> getDataClass();

	Class<?> getBuilderClass();

	List<PropertyNode<?>> getProperties();

	boolean isPrimitive();
}
