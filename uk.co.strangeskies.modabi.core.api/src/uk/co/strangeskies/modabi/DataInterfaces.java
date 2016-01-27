package uk.co.strangeskies.modabi;

import java.util.Set;

import uk.co.strangeskies.modabi.io.structured.StructuredDataFormat;

public interface DataInterfaces {
	void registerDataInterface(StructuredDataFormat handler);

	void unregisterDataInterface(StructuredDataFormat handler);

	Set<StructuredDataFormat> getRegisteredDataInterfaces();

	StructuredDataFormat getDataInterface(String id);

	Set<StructuredDataFormat> getDataInterfaces(String extension);
}
