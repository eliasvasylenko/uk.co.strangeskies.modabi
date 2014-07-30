package uk.co.strangeskies.modabi.model;

import java.util.List;

import uk.co.strangeskies.modabi.model.nodes.BindingNode;

public interface AbstractModel<T, E extends AbstractModel.Effective<T, E>>
		extends BindingNode<T, E> {
	interface Effective<T, E extends AbstractModel.Effective<T, E>> extends
			AbstractModel<T, E>, BindingNode.Effective<T, E> {
		@Override
		List<Model.Effective<? super T>> baseModel();
	}

	Boolean isAbstract();

	List<? extends Model<? super T>> baseModel();
}
