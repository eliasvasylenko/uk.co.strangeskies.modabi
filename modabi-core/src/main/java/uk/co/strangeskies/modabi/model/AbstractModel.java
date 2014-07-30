package uk.co.strangeskies.modabi.model;

import java.util.List;

import uk.co.strangeskies.modabi.model.nodes.BindingNode;

public interface AbstractModel<T, E extends AbstractModel<T, E> & BindingNode.Effective<T, E>>
		extends BindingNode<T, E> {
	public Boolean isAbstract();

	public List<Model<? super T>> baseModel();
}
