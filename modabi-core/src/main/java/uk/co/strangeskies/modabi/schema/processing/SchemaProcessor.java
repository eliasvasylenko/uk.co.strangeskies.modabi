package uk.co.strangeskies.modabi.schema.processing;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import uk.co.strangeskies.modabi.schema.BindingSchema;

public interface SchemaProcessor {
	public <T> T processInput(BindingSchema<T> schema, InputStream input);

	public <T> void processOutput(T data, BindingSchema<T> schema,
			OutputStream output);

	public String getFormatName();

	public List<String> getFileExtentions();
}
