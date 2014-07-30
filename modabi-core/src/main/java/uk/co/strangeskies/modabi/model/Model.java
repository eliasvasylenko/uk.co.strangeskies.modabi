package uk.co.strangeskies.modabi.model;

import uk.co.strangeskies.modabi.model.nodes.BindingNode;

public interface Model<T> extends AbstractModel<T, Model.Effective<T>> {
	interface Effective<T> extends Model<T>,
			BindingNode.Effective<T, Effective<T>> {
	}
}
