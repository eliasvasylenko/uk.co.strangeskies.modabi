package uk.co.strangeskies.modabi.model.nodes;

import uk.co.strangeskies.modabi.data.DataBindingType;
import uk.co.strangeskies.modabi.data.io.BufferedDataSource;
import uk.co.strangeskies.modabi.schema.processing.SchemaProcessingContext;
import uk.co.strangeskies.modabi.schema.processing.ValueResolution;

public interface DataNode<T> extends
		BindingChildNode<T, DataNode.Effective<T>>,
		DataNodeChildNode<DataNode.Effective<T>> {
	interface Effective<T> extends DataNode<T>,
			BindingChildNode.Effective<T, Effective<T>>,
			DataNodeChildNode<Effective<T>> {
		@Override
		default void process(SchemaProcessingContext context) {
			context.accept(this);
		}

		@Override
		DataBindingType.Effective<T> type();
	}

	enum Format {
		PROPERTY, CONTENT, SIMPLE_ELEMENT
	}

	Format format();

	default boolean isValueProvided() {
		return (valueResolution() == ValueResolution.PROCESSING_TIME && providedValueBuffer() != null)
				|| (valueResolution() == ValueResolution.REGISTRATION_TIME && providedValue() != null);
	}

	BufferedDataSource providedValueBuffer();

	T providedValue();

	ValueResolution valueResolution();

	DataBindingType<T> type();

	Boolean optional();
}
