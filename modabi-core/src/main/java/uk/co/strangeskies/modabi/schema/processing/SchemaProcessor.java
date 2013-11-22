package uk.co.strangeskies.modabi.schema.processing;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import uk.co.strangeskies.modabi.schema.Schema;

public interface SchemaProcessor<U extends DataInput<? extends U>> {
	public <T> T processInput(Schema<T, ? super U> schema, InputStream input);

	public <T> void processOutput(T data, Schema<T, ? super U> schema,
			OutputStream output);

	public String getFormatName();

	public List<String> getFileExtentions();
}
