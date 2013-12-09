package uk.co.strangeskies.modabi.processing;

import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.data.DataInput;
import uk.co.strangeskies.modabi.data.DataOutput;

public interface SchemaBinder {
	public <T> T processInput(Schema<T> schema, DataInput input);

	public Object processInput(DataInput input);

	public <T> void processOutput(T data, Schema<T> schema, DataOutput output);

	public void processOutput(Object data, DataOutput output);
}
