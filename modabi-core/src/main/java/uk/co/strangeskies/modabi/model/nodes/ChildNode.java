package uk.co.strangeskies.modabi.model.nodes;

import uk.co.strangeskies.gears.utilities.PropertySet;
import uk.co.strangeskies.modabi.schema.processing.SchemaProcessingContext;

public interface ChildNode<S extends ChildNode<S, E>, E extends ChildNode.Effective<S, E>>
		extends SchemaNode<S, E> {
	interface Effective<S extends ChildNode<S, E>, E extends Effective<S, E>>
			extends ChildNode<S, E>, SchemaNode.Effective<S, E> {
		Class<?> getPreInputClass();

		Class<?> getPostInputClass();

		void process(SchemaProcessingContext context);

		@Override
		default PropertySet<E> effectivePropertySet() {
			return SchemaNode.Effective.super.effectivePropertySet()
					.add(ChildNode.Effective::getPreInputClass)
					.add(ChildNode.Effective::getPostInputClass);
		}
	}
}
