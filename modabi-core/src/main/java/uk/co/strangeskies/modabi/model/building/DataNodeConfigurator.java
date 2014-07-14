package uk.co.strangeskies.modabi.model.building;

import uk.co.strangeskies.modabi.data.DataBindingType;
import uk.co.strangeskies.modabi.data.io.BufferedDataSource;
import uk.co.strangeskies.modabi.model.nodes.DataNode;
import uk.co.strangeskies.modabi.model.nodes.DataNode.Format;
import uk.co.strangeskies.modabi.model.nodes.DataNode.Value.ValueResolution;
import uk.co.strangeskies.modabi.model.nodes.DataNodeChildNode;

public interface DataNodeConfigurator<T>
		extends
		BindingChildNodeConfigurator<DataNodeConfigurator<T>, DataNode<T>, T>,
		BranchingNodeConfigurator<DataNodeConfigurator<T>, DataNode<T>, DataNodeChildNode, DataNode<?>> {
	public <U extends T> DataNodeConfigurator<U> type(DataBindingType<U> type);

	@Override
	public <U extends T> DataNodeConfigurator<U> dataClass(Class<U> dataClass);

	public DataNodeConfigurator<T> provideValue(BufferedDataSource dataSource);

	public DataNodeConfigurator<T> valueResolution(ValueResolution valueResolution);

	public DataNodeConfigurator<T> optional(boolean optional);

	public DataNodeConfigurator<T> format(Format format);
}
