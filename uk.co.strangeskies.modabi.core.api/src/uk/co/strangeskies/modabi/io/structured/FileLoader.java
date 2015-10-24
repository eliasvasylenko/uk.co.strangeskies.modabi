package uk.co.strangeskies.modabi.io.structured;

import java.io.InputStream;

public interface FileLoader {
	boolean isValidForExtension(String extension);

	StructuredDataSource loadFile(InputStream file);
}
