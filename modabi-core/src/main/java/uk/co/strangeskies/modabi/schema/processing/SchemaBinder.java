package uk.co.strangeskies.modabi.schema.processing;

import java.util.function.Function;
import java.util.function.Supplier;

import uk.co.strangeskies.modabi.data.io.structured.StructuredDataSource;
import uk.co.strangeskies.modabi.data.io.structured.StructuredDataTarget;
import uk.co.strangeskies.modabi.model.Model;
import uk.co.strangeskies.modabi.schema.BaseSchema;
import uk.co.strangeskies.modabi.schema.Binding;
import uk.co.strangeskies.modabi.schema.MetaSchema;

public interface SchemaBinder {
	public <T> void registerProvider(Class<T> providedClass, Supplier<T> provider);

	public void registerProvider(Function<Class<?>, ?> provider);

	public <T> T processInput(Model<T> model, StructuredDataSource input);

	public Binding<?> processInput(StructuredDataSource input);

	public <T> void processOutput(Model<T> model, StructuredDataTarget output, T data);

	public <T> void processOutput(StructuredDataTarget output, T data);

	public MetaSchema getMetaSchema();

	public BaseSchema getBaseSchema();
}
