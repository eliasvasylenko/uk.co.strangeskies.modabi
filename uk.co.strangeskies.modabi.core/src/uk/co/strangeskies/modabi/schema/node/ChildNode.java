package uk.co.strangeskies.modabi.schema.node;

import java.lang.reflect.Type;

import uk.co.strangeskies.modabi.schema.management.SchemaProcessingContext;
import uk.co.strangeskies.utilities.PropertySet;

public interface ChildNode<S extends ChildNode<S, E>, E extends ChildNode.Effective<S, E>>
		extends SchemaNode<S, E> {
	interface Effective<S extends ChildNode<S, E>, E extends Effective<S, E>>
			extends ChildNode<S, E>, SchemaNode.Effective<S, E> {
		Type getPreInputType();

		void process(SchemaProcessingContext context);

		@Override
		default PropertySet<E> effectivePropertySet() {
			return SchemaNode.Effective.super.effectivePropertySet().add(
					ChildNode.Effective::getPreInputType);
		}
	}

	@Override
	public default PropertySet<S> propertySet() {
		return SchemaNode.super.propertySet().add(ChildNode::getPostInputType);
	}

	Type getPostInputType();
}
