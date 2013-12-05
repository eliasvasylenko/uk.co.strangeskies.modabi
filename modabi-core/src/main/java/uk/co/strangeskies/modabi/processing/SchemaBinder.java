package uk.co.strangeskies.modabi.processing;

import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.data.DataInput;
import uk.co.strangeskies.modabi.data.DataOutput;

public interface SchemaBinder<U extends SchemaProcessingContext<? extends U>> {
	public void registerSchema(Schema<?, ? super U> schema);

	public <T> T processInput(Schema<T, ? super U> schema, DataInput input);

	public Object processInput(DataInput input);

	public <T> void processOutput(T data, Schema<T, ? super U> schema,
			DataOutput output);

	public void processOutput(Object data, DataOutput output);
}
