package uk.co.strangeskies.modabi.model;

import java.util.List;

import uk.co.strangeskies.modabi.model.nodes.BindingNode;

public interface AbstractModel<T> extends BindingNode<T> {
	public Boolean isAbstract();

	public List<Model<? super T>> getBaseModel();
}
