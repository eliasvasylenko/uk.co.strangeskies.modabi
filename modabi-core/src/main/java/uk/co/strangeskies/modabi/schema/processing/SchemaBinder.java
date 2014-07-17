package uk.co.strangeskies.modabi.schema.processing;

import java.util.function.Function;
import java.util.function.Supplier;

import uk.co.strangeskies.modabi.data.io.structured.StructuredDataSource;
import uk.co.strangeskies.modabi.data.io.structured.StructuredDataTarget;
import uk.co.strangeskies.modabi.model.Model;
import uk.co.strangeskies.modabi.schema.BaseSchema;
import uk.co.strangeskies.modabi.schema.Binding;
import uk.co.strangeskies.modabi.schema.MetaSchema;
import uk.co.strangeskies.modabi.schema.Schema;

public interface SchemaBinder {
	public <T> void registerProvider(Class<T> providedClass, Supplier<T> provider);

	public void registerProvider(Function<Class<?>, ?> provider);

	public void registerSchema(Schema schema);

	public <T> T processInput(Model<T> model, StructuredDataSource input);

	public Binding<?> processInput(StructuredDataSource input);

	public default Schema registerSchemaInput(StructuredDataSource input) {
		Schema schema = processInput(getMetaSchema().getSchemaModel(), input);

		registerSchema(schema);

		return schema;
	}

	public <T> void processOutput(Model<T> model, StructuredDataTarget output,
			T data);

	public MetaSchema getMetaSchema();

	public BaseSchema getBaseSchema();
}
