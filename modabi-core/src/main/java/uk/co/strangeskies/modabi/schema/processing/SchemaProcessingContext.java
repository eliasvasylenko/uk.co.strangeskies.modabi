package uk.co.strangeskies.modabi.schema.processing;

import uk.co.strangeskies.modabi.schema.node.BranchSchemaNode;
import uk.co.strangeskies.modabi.schema.node.DataSchemaNode;
import uk.co.strangeskies.modabi.schema.node.ElementSchemaNode;
import uk.co.strangeskies.modabi.schema.node.PropertySchemaNode;

public interface SchemaProcessingContext<T extends SchemaProcessingContext<T>> {
	public void data(DataSchemaNode<?> node);

	public void property(PropertySchemaNode<?> node);

	public void branch(BranchSchemaNode<? super T> node);

	public void element(ElementSchemaNode<?, ? super T> node);
}
