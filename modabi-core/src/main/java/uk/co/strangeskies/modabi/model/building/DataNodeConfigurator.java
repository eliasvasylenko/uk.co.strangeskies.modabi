package uk.co.strangeskies.modabi.model.building;

import uk.co.strangeskies.modabi.data.DataType;
import uk.co.strangeskies.modabi.data.io.BufferedDataSource;
import uk.co.strangeskies.modabi.model.nodes.DataNode;
import uk.co.strangeskies.modabi.model.nodes.DataNode.Format;
import uk.co.strangeskies.modabi.model.nodes.DataNodeChildNode;

public interface DataNodeConfigurator<T>
		extends
		BindingChildNodeConfigurator<DataNodeConfigurator<T>, DataNode<T>, T>,
		BranchingNodeConfigurator<DataNodeConfigurator<T>, DataNode<T>, DataNodeChildNode, DataNode<?>> {
	public <U extends T> DataNodeConfigurator<U> type(DataType<U> type);

	@Override
	public <U extends T> DataNodeConfigurator<U> dataClass(Class<U> dataClass);

	public DataNodeConfigurator<T> value(BufferedDataSource dataSource);

	public DataNodeConfigurator<T> optional(boolean optional);

	public DataNodeConfigurator<T> format(Format format);
}
