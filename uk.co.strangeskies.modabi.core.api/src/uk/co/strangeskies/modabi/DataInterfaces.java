package uk.co.strangeskies.modabi;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.io.structured.StructuredDataFormat;

public interface DataInterfaces {
	void registerDataInterface(StructuredDataFormat handler);

	void unregisterDataInterface(StructuredDataFormat handler);

	Set<StructuredDataFormat> getRegisteredDataInterfaces();

	StructuredDataFormat getDataInterface(String id);

	default Set<StructuredDataFormat> getDataInterfaces(String extension) {
		return getRegisteredDataInterfaces().stream()
				.filter(l -> l.getFileExtensions().contains(extension))
				.collect(Collectors.toCollection(LinkedHashSet::new));
	}
}
