package uk.co.strangeskies.modabi.node.builder;

import uk.co.strangeskies.gears.utilities.Factory;
import uk.co.strangeskies.modabi.node.SchemaNode;
import uk.co.strangeskies.modabi.processing.SchemaProcessingContext;

public interface SchemaNodeBuilder<T extends SchemaNode<U>, U extends SchemaProcessingContext<? extends U>>
		extends Factory<T> {
}
