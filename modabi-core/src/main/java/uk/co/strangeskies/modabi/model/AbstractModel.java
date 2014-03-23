package uk.co.strangeskies.modabi.model;

import java.util.List;

import uk.co.strangeskies.modabi.model.nodes.BranchingNode;
import uk.co.strangeskies.modabi.processing.BindingStrategy;

public interface AbstractModel<T> extends BranchingNode {
	public Boolean isAbstract();

	public List<Model<? super T>> getBaseModel();

	public Class<T> getDataClass();

	public BindingStrategy getImplementationStrategy();

	public Class<?> getBuilderClass();
}
