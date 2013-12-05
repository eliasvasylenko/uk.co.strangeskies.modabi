package uk.co.strangeskies.modabi;

import uk.co.strangeskies.modabi.node.builder.SchemaNodeBuilderFactory;
import uk.co.strangeskies.modabi.processing.SchemaProcessingContext;

public interface MetaSchemaFactory<T extends SchemaProcessingContext<? extends T>, U extends SchemaProcessingContext<? extends U>> {
	public Schema<Schema<?, T>, U> create(
			SchemaNodeBuilderFactory factory);
}
