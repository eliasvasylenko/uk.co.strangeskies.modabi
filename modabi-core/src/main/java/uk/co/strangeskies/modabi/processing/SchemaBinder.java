package uk.co.strangeskies.modabi.processing;

import uk.co.strangeskies.modabi.data.StructuredDataInput;
import uk.co.strangeskies.modabi.data.StructuredDataOutput;
import uk.co.strangeskies.modabi.model.Binding;
import uk.co.strangeskies.modabi.model.Model;

public interface SchemaBinder {
	public Binding<?> processInput(StructuredDataInput input);

	public <T> T processInput(Model<T> model, StructuredDataInput input);

	public <T> void processOutput(Model<T> model, StructuredDataOutput output, T data);
}
