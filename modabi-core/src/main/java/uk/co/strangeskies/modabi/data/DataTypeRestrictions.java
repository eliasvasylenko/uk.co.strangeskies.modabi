package uk.co.strangeskies.modabi.data;

import java.util.List;

import uk.co.strangeskies.modabi.model.nodes.PropertyNode;

public interface DataTypeRestrictions<T> {
	DataTypeRestrictions<?> getBaseRestrictions();

	List<PropertyNode<?>> getProperties();
}
