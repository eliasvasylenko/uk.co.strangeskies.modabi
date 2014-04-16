package uk.co.strangeskies.modabi.processing;

import uk.co.strangeskies.modabi.BaseSchema;
import uk.co.strangeskies.modabi.Binding;
import uk.co.strangeskies.modabi.MetaSchema;
import uk.co.strangeskies.modabi.data.io.structured.StructuredInput;
import uk.co.strangeskies.modabi.data.io.structured.StructuredOutput;
import uk.co.strangeskies.modabi.model.Model;

public interface SchemaBinder {
	public <T> T processInput(Model<T> model, StructuredInput input);

	public Binding<?> processInput(StructuredInput input);

	public <T> void processOutput(Model<T> model, StructuredOutput output,
			T data);

	public <T> void processOutput(StructuredOutput output, T data);

	public MetaSchema getMetaSchema();

	public BaseSchema getBaseSchema();
}
