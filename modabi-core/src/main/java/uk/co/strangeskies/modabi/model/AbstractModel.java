package uk.co.strangeskies.modabi.model;

import java.util.Set;

import uk.co.strangeskies.modabi.model.nodes.BindingNode;

public interface AbstractModel<T, S extends AbstractModel<T, S, E>, E extends AbstractModel.Effective<T, S, E>>
		extends BindingNode<T, S, E> {
	interface Effective<T, S extends AbstractModel<T, S, E>, E extends AbstractModel.Effective<T, S, E>>
			extends AbstractModel<T, S, E>, BindingNode.Effective<T, S, E> {
		@Override
		Set<Model.Effective<? super T>> baseModel();
	}

	Boolean isAbstract();

	Set<? extends Model<? super T>> baseModel();
}
