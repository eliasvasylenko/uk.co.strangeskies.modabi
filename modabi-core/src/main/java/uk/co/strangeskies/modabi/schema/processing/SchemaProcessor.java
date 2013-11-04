package uk.co.strangeskies.modabi.schema.processing;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import uk.co.strangeskies.modabi.schema.SchemaGraph;

public interface SchemaProcessor {
	public <T> T processInput(SchemaGraph<T> schema, InputStream input);

	public <T> void processOutput(T data, SchemaGraph<T> schema,
			OutputStream output);

	public String getFormatName();

	public List<String> getFileExtentions();
}
