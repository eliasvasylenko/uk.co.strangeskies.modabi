package uk.co.strangeskies.modabi.schema.model.building.configurators;

import uk.co.strangeskies.modabi.data.DataBindingType;
import uk.co.strangeskies.modabi.data.io.DataSource;
import uk.co.strangeskies.modabi.schema.model.nodes.DataNode;
import uk.co.strangeskies.modabi.schema.model.nodes.DataNodeChildNode;
import uk.co.strangeskies.modabi.schema.model.nodes.DataNode.Format;
import uk.co.strangeskies.modabi.schema.processing.ValueResolution;

public interface DataNodeConfigurator<T>
		extends
		BindingChildNodeConfigurator<DataNodeConfigurator<T>, DataNode<T>, T, DataNodeChildNode<?, ?>, DataNode<?>>,
		SchemaNodeConfigurator<DataNodeConfigurator<T>, DataNode<T>, DataNodeChildNode<?, ?>, DataNode<?>> {
	public <U extends T> DataNodeConfigurator<U> type(DataBindingType<U> type);

	@Override
	public <U extends T> DataNodeConfigurator<U> dataClass(Class<U> dataClass);

	public DataNodeConfigurator<T> provideValue(DataSource dataSource);

	public DataNodeConfigurator<T> valueResolution(ValueResolution valueResolution);

	public DataNodeConfigurator<T> optional(boolean optional);

	public DataNodeConfigurator<T> format(Format format);
}
