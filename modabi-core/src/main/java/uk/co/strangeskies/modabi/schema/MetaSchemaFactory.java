package uk.co.strangeskies.modabi.schema;

import uk.co.strangeskies.modabi.schema.node.builder.SchemaNodeBuilderFactory;
import uk.co.strangeskies.modabi.schema.processing.DataInput;

public interface MetaSchemaFactory<T extends DataInput<? extends T>, U extends DataInput<? extends U>> {
	public Schema<Schema<?, T>, U> create(
			SchemaNodeBuilderFactory factory);
}
