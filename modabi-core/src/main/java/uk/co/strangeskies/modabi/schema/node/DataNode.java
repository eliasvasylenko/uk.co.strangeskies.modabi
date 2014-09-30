package uk.co.strangeskies.modabi.schema.node;

import java.util.List;

import uk.co.strangeskies.mathematics.Range;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.node.type.DataBindingType;
import uk.co.strangeskies.modabi.schema.processing.SchemaProcessingContext;
import uk.co.strangeskies.modabi.schema.processing.ValueResolution;
import uk.co.strangeskies.utilities.PropertySet;

public interface DataNode<T> extends
		BindingChildNode<T, DataNode<T>, DataNode.Effective<T>>,
		DataNodeChildNode<DataNode<T>, DataNode.Effective<T>> {
	interface Effective<T> extends DataNode<T>,
			BindingChildNode.Effective<T, DataNode<T>, Effective<T>>,
			DataNodeChildNode<DataNode<T>, Effective<T>> {
		@Override
		default void process(SchemaProcessingContext context) {
			context.accept(this);
		}

		@Override
		DataBindingType.Effective<T> type();

		List<T> providedValues();

		default T providedValue() {
			if (!Range.create(0, 1).contains(occurrences()))
				throw new SchemaException("Cannot request single value from node '"
						+ getName() + "' with occurrences '" + occurrences() + "'.");

			if (providedValues() == null || providedValues().isEmpty())
				return null;
			else
				return providedValues().get(0);
		}
	}

	@Override
	default PropertySet<DataNode<T>> propertySet() {
		return BindingChildNode.super.propertySet().add(DataNode::format)
				.add(DataNode::providedValueBuffer).add(DataNode::valueResolution)
				.add(DataNode::type).add(DataNode::optional)
				.add(DataNode::isExtensible);
	}

	enum Format {
		PROPERTY, CONTENT, SIMPLE_ELEMENT
	}

	Format format();

	default boolean isValueProvided() {
		return providedValueBuffer() != null;
	}

	DataSource providedValueBuffer();

	ValueResolution valueResolution();

	DataBindingType<T> type();

	Boolean optional();

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	default Class<Effective<T>> getEffectiveClass() {
		return (Class) DataNode.Effective.class;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	default Class<DataNode<T>> getNodeClass() {
		return (Class) DataNode.class;
	}
}
