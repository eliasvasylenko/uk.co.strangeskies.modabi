package uk.co.strangeskies.modabi.schema.node;

import java.util.List;

import uk.co.strangeskies.modabi.schema.node.model.Model;
import uk.co.strangeskies.utilities.PropertySet;

public interface AbstractComplexNode<T, S extends AbstractComplexNode<T, S, E>, E extends AbstractComplexNode.Effective<T, S, E>>
		extends BindingNode<T, S, E> {
	interface Effective<T, S extends AbstractComplexNode<T, S, E>, E extends AbstractComplexNode.Effective<T, S, E>>
			extends AbstractComplexNode<T, S, E>, BindingNode.Effective<T, S, E> {
		@Override
		List<Model.Effective<? super T>> baseModel();
	}

	@Override
	default PropertySet<S> propertySet() {
		return BindingNode.super.propertySet().add(AbstractComplexNode::baseModel);
	}

	List<? extends Model<? super T>> baseModel();
}
