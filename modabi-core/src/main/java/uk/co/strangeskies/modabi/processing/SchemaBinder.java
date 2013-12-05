package uk.co.strangeskies.modabi.schema.processing;

import uk.co.strangeskies.modabi.schema.Schema;
import uk.co.strangeskies.modabi.schema.data.DataInput;
import uk.co.strangeskies.modabi.schema.data.DataOutput;

public interface SchamaBinder<U extends SchemaProcessingContext<? extends U>> {
	public <T> T processInput(Schema<T, ? super U> schema, DataInput input);

	public <T> void processOutput(T data, Schema<T, ? super U> schema,
			DataOutput output);
}
