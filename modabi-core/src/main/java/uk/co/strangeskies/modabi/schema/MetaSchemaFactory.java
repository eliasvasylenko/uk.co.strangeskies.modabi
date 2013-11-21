package uk.co.strangeskies.modabi.schema;

import uk.co.strangeskies.modabi.schema.node.builder.SchemaNodeBuilderFactory;
import uk.co.strangeskies.modabi.schema.processing.SchemaProcessingContext;

public interface MetaSchemaFactory<T extends SchemaProcessingContext<? extends T>, U extends SchemaProcessingContext<? extends U>> {
	public Schema<Schema<?, T>, U> create(
			SchemaNodeBuilderFactory factory);
}
