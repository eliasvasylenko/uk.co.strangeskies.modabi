package uk.co.strangeskies.modabi.model.nodes;

import java.util.List;

import uk.co.strangeskies.gears.mathematics.Range;
import uk.co.strangeskies.modabi.data.DataBindingType;
import uk.co.strangeskies.modabi.data.io.BufferedDataSource;
import uk.co.strangeskies.modabi.schema.processing.BindingStrategy;
import uk.co.strangeskies.modabi.schema.processing.SchemaProcessingContext;
import uk.co.strangeskies.modabi.schema.processing.UnbindingStrategy;
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

	static <T> DataNode<T> wrapType(DataNode<T> node) {
		if (node.type() == null)
			return node;

		return new DataNode<T>() {
			@Override
			public BufferedDataSource providedValueBuffer() {
				return node.providedValueBuffer();
			}

			@Override
			public T providedValue() {
				return node.providedValue();
			}

			@Override
			public ValueResolution valueResolution() {
				return node.valueResolution();
			}

			@Override
			public String getOutMethodName() {
				return node.getOutMethodName();
			}

			@Override
			public Boolean isOutMethodIterable() {
				return node.isOutMethodIterable();
			}

			@Override
			public Range<Integer> occurances() {
				return node.occurances();
			}

			@Override
			public Class<T> getDataClass() {
				if (node.type().effective().getDataClass() != null
						&& (node.getDataClass() == null || node.getDataClass()
								.isAssignableFrom(node.type().effective().getDataClass())))
					return node.type().effective().getDataClass();
				else
					return node.getDataClass();
			}

			@Override
			public BindingStrategy getBindingStrategy() {
				return node.type().effective().getBindingStrategy();
			}

			@Override
			public Class<?> getBindingClass() {
				if (node.type().effective().getBindingClass() != null
						&& (node.getBindingClass() == null || node.getBindingClass()
								.isAssignableFrom(node.type().effective().getBindingClass())))
					return node.type().effective().getBindingClass();
				else
					return node.getBindingClass();
			}

			@Override
			public UnbindingStrategy getUnbindingStrategy() {
				return node.type().effective().getUnbindingStrategy();
			}

			@Override
			public Class<?> getUnbindingClass() {
				if (node.type().effective().getUnbindingClass() != null
						&& (node.getUnbindingClass() == null || node.getUnbindingClass()
								.isAssignableFrom(node.type().effective().getUnbindingClass())))
					return node.type().effective().getUnbindingClass();
				else
					return node.getUnbindingClass();
			}

			@Override
			public Class<?> getUnbindingFactoryClass() {
				if (node.type().effective().getUnbindingFactoryClass() != null
						&& (node.getUnbindingFactoryClass() == null || node
								.getUnbindingFactoryClass().isAssignableFrom(
										node.type().effective().getUnbindingFactoryClass())))
					return node.type().effective().getUnbindingFactoryClass();
				else
					return node.getUnbindingFactoryClass();
			}

			@Override
			public String getUnbindingMethodName() {
				return node.type().effective().getUnbindingMethodName();
			}

			@Override
			public String getId() {
				return node.getId() != null ? node.getId() : node.type().effective()
						.getName();
			}

			@Override
			public List<? extends ChildNode<?>> children() {
				return node.type().children();
			}

			@Override
			public String getInMethodName() {
				return node.getInMethodName();
			}

			@Override
			public Boolean isInMethodChained() {
				return node.isInMethodChained();
			}

			@Override
			public Format format() {
				return node.format();
			}

			@Override
			public DataBindingType<T> type() {
				return node.type();
			}

			@Override
			public Boolean optional() {
				return node.optional();
			}

			@Override
			public Effective<T> effective() {
				return node.effective();
			}
		};
	}
}
