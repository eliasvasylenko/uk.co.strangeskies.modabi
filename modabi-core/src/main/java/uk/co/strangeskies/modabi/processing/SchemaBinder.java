package uk.co.strangeskies.modabi.processing;

import uk.co.strangeskies.modabi.BaseSchema;
import uk.co.strangeskies.modabi.Binding;
import uk.co.strangeskies.modabi.MetaSchema;
import uk.co.strangeskies.modabi.data.io.structured.StructuredDataInput;
import uk.co.strangeskies.modabi.data.io.structured.StructuredDataOutput;
import uk.co.strangeskies.modabi.model.Model;

public interface SchemaBinder {
	public <T> T processInput(Model<T> model, StructuredDataInput input);

	public Binding<?> processInput(StructuredDataInput input);

	public <T> void processOutput(Model<T> model, StructuredDataOutput output,
			T data);

	public <T> void processOutput(StructuredDataOutput output, T data);

	public MetaSchema getMetaSchema();

	public BaseSchema getBaseSchema();
}
