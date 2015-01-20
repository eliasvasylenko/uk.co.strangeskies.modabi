package uk.co.strangeskies.modabi.schema.node.building.configuration;

import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.schema.management.ValueResolution;
import uk.co.strangeskies.modabi.schema.node.DataNode;
import uk.co.strangeskies.modabi.schema.node.DataNode.Format;
import uk.co.strangeskies.modabi.schema.node.type.DataBindingType;
import uk.co.strangeskies.reflection.TypeLiteral;

public interface DataNodeConfigurator<T> extends
		BindingChildNodeConfigurator<DataNodeConfigurator<T>, DataNode<T>, T>,
		SchemaNodeConfigurator<DataNodeConfigurator<T>, DataNode<T>> {
	public <U extends T> DataNodeConfigurator<U> type(
			DataBindingType<? super U> type);

	@SuppressWarnings("unchecked")
	@Override
	default public <U extends T> DataNodeConfigurator<U> dataClass(
			Class<U> dataClass) {
		return (DataNodeConfigurator<U>) BindingChildNodeConfigurator.super
				.dataClass(dataClass);
	}

	@Override
	public <U extends T> DataNodeConfigurator<U> dataType(TypeLiteral<U> dataClass);

	public DataNodeConfigurator<T> provideValue(DataSource dataSource);

	public DataNodeConfigurator<T> valueResolution(ValueResolution valueResolution);

	public DataNodeConfigurator<T> optional(boolean optional);

	public DataNodeConfigurator<T> nullIfOmitted(boolean nullIfOmitted);

	public DataNodeConfigurator<T> format(Format format);
}
