package uk.co.strangeskies.modabi.model.nodes;

import uk.co.strangeskies.gears.utilities.PropertySet;
import uk.co.strangeskies.modabi.model.AbstractModel;
import uk.co.strangeskies.modabi.schema.processing.SchemaProcessingContext;

public interface ElementNode<T> extends
		AbstractModel<T, ElementNode<T>, ElementNode.Effective<T>>,
		BindingChildNode<T, ElementNode<T>, ElementNode.Effective<T>> {
	interface Effective<T> extends ElementNode<T>,
			AbstractModel.Effective<T, ElementNode<T>, Effective<T>>,
			BindingChildNode.Effective<T, ElementNode<T>, Effective<T>> {
		@Override
		default void process(SchemaProcessingContext context) {
			context.accept(this);
		}
	}

	@Override
	default PropertySet<ElementNode<T>> propertySet() {
		return BindingChildNode.super.propertySet().add(AbstractModel::baseModel);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	default Class<Effective<T>> getEffectiveClass() {
		return (Class) Effective.class;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	default Class<ElementNode<T>> getNodeClass() {
		return (Class) ElementNode.class;
	}
}
