package uk.co.strangeskies.modabi.schema.node.model;

import uk.co.strangeskies.modabi.schema.node.AbstractComplexNode;

public interface Model<T> extends
		AbstractComplexNode<T, Model<T>, Model.Effective<T>> {
	interface Effective<T> extends Model<T>,
			AbstractComplexNode.Effective<T, Model<T>, Effective<T>> {
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	default Class<Effective<T>> getEffectiveClass() {
		return (Class) Effective.class;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	default Class<Model<T>> getNodeClass() {
		return (Class) Model.class;
	}
}
