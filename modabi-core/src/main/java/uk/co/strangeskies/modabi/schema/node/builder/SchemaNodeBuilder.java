package uk.co.strangeskies.modabi.schema.node.builder;

import uk.co.strangeskies.gears.utilities.Factory;
import uk.co.strangeskies.modabi.schema.node.SchemaNode;
import uk.co.strangeskies.modabi.schema.processing.DataInput;

public interface SchemaNodeBuilder<T extends SchemaNode<U>, U extends DataInput<? extends U>>
		extends Factory<T> {
}
