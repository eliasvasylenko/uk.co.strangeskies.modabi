package uk.co.strangeskies.modabi.schema.node;

import uk.co.strangeskies.modabi.schema.processing.SchemaProcessingContext;
import uk.co.strangeskies.utilities.PropertySet;

public interface ComplexNode<T> extends
		AbstractComplexNode<T, ComplexNode<T>, ComplexNode.Effective<T>>,
		BindingChildNode<T, ComplexNode<T>, ComplexNode.Effective<T>> {
	interface Effective<T> extends ComplexNode<T>,
			AbstractComplexNode.Effective<T, ComplexNode<T>, Effective<T>>,
			BindingChildNode.Effective<T, ComplexNode<T>, Effective<T>> {
		@Override
		default void process(SchemaProcessingContext context) {
			context.accept(this);
		}
	}

	Boolean isInline();

	@Override
	default PropertySet<ComplexNode<T>> propertySet() {
		return BindingChildNode.super.propertySet().add(
				AbstractComplexNode::baseModel);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	default Class<Effective<T>> getEffectiveClass() {
		return (Class) Effective.class;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	default Class<ComplexNode<T>> getNodeClass() {
		return (Class) ComplexNode.class;
	}
}
