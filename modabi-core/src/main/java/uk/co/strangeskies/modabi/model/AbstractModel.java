package uk.co.strangeskies.modabi.model;

import java.util.List;

public interface AbstractModel<T> extends BranchingNode {
	public Boolean isAbstract();

	public List<Model<? super T>> getBaseModel();

	public Class<T> getDataClass();

	public ImplementationStrategy getImplementationStrategy();

	public Class<?> getBuilderClass();

	/**
	 * The method to call to instantiate the data object in the case that this
	 * element node has an associated builder class as returned by
	 * {@link #getBuilderClass()}. If no build method is specified, this method
	 * will return null. In that case the very last child of this class which has
	 * an associated inMethod will be checked for an appropriate
	 *
	 * @return
	 */
	public String getBuilderMethod();
}
