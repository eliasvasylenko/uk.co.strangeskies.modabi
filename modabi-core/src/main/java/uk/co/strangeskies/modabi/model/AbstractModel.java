package uk.co.strangeskies.modabi.model;

import java.util.Set;

import uk.co.strangeskies.modabi.model.nodes.BindingNode;

public interface AbstractModel<T, E extends AbstractModel.Effective<T, E>>
		extends BindingNode<T, E> {
	interface Effective<T, E extends AbstractModel.Effective<T, E>> extends
			AbstractModel<T, E>, BindingNode.Effective<T, E> {
		@Override
		Set<Model.Effective<? super T>> baseModel();
	}

	Boolean isAbstract();

	Set<? extends Model<? super T>> baseModel();
}
