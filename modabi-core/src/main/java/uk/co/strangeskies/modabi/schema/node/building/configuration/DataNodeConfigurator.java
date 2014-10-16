package uk.co.strangeskies.modabi.schema.node.building.configuration;

import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.schema.node.DataNode;
import uk.co.strangeskies.modabi.schema.node.DataNodeChildNode;
import uk.co.strangeskies.modabi.schema.node.DataNode.Format;
import uk.co.strangeskies.modabi.schema.node.type.DataBindingType;
import uk.co.strangeskies.modabi.schema.processing.ValueResolution;

public interface DataNodeConfigurator<T>
		extends
		BindingChildNodeConfigurator<DataNodeConfigurator<T>, DataNode<T>, T, DataNodeChildNode<?, ?>, DataNode<?>>,
		SchemaNodeConfigurator<DataNodeConfigurator<T>, DataNode<T>, DataNodeChildNode<?, ?>, DataNode<?>> {
	public <U extends T> DataNodeConfigurator<U> type(
			DataBindingType<? super U> type);

	@Override
	public <U extends T> DataNodeConfigurator<U> dataClass(Class<U> dataClass);

	public DataNodeConfigurator<T> provideValue(DataSource dataSource);

	public DataNodeConfigurator<T> valueResolution(ValueResolution valueResolution);

	public DataNodeConfigurator<T> optional(boolean optional);

	public DataNodeConfigurator<T> format(Format format);
}
