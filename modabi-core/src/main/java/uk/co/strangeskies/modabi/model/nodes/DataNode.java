package uk.co.strangeskies.modabi.model.nodes;

import java.lang.reflect.Method;
import java.util.List;

import uk.co.strangeskies.gears.mathematics.Range;
import uk.co.strangeskies.modabi.data.DataBindingType;
import uk.co.strangeskies.modabi.data.io.BufferedDataSource;
import uk.co.strangeskies.modabi.schema.processing.BindingStrategy;
import uk.co.strangeskies.modabi.schema.processing.SchemaProcessingContext;
import uk.co.strangeskies.modabi.schema.processing.UnbindingStrategy;
import uk.co.strangeskies.modabi.schema.processing.ValueResolution;

public interface DataNode<T> extends BindingChildNode<T>, DataNodeChildNode {
	public enum Format {
		PROPERTY, CONTENT, SIMPLE_ELEMENT
	}

	Format format();

	default boolean isValuePresent() {
		return (valueResolution() == ValueResolution.PROCESSING_TIME && providedValueBuffer() != null)
				|| (valueResolution() == ValueResolution.REGISTRATION_TIME && providedValue() != null);
	}

	BufferedDataSource providedValueBuffer();

	T providedValue();

	ValueResolution valueResolution();

	DataBindingType<T> type();

	Boolean optional();

	@Override
	default void process(SchemaProcessingContext context) {
		context.accept(this);
	}

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
			public Method getOutMethod() {
				return node.getOutMethod();
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
				if (node.type().getDataClass() != null
						&& (node.getDataClass() == null || node.getDataClass()
								.isAssignableFrom(node.type().getDataClass())))
					return node.type().getDataClass();
				else
					return node.getDataClass();
			}

			@Override
			public BindingStrategy getBindingStrategy() {
				return node.type().getBindingStrategy();
			}

			@Override
			public Class<?> getBindingClass() {
				if (node.type().getBindingClass() != null
						&& (node.getBindingClass() == null || node.getBindingClass()
								.isAssignableFrom(node.type().getBindingClass())))
					return node.type().getBindingClass();
				else
					return node.getBindingClass();
			}

			@Override
			public UnbindingStrategy getUnbindingStrategy() {
				return node.type().getUnbindingStrategy();
			}

			@Override
			public Class<?> getUnbindingClass() {
				if (node.type().getUnbindingClass() != null
						&& (node.getUnbindingClass() == null || node.getUnbindingClass()
								.isAssignableFrom(node.type().getUnbindingClass())))
					return node.type().getUnbindingClass();
				else
					return node.getUnbindingClass();
			}

			@Override
			public Method getUnbindingMethod() {
				return node.type().getUnbindingMethod();
			}

			@Override
			public String getUnbindingMethodName() {
				return node.type().getUnbindingMethodName();
			}

			@Override
			public String getId() {
				return node.getId() != null ? node.getId() : node.type().getName();
			}

			@Override
			public List<? extends ChildNode> getChildren() {
				return node.type().getChildren();
			}

			@Override
			public String getInMethodName() {
				return node.getInMethodName();
			}

			@Override
			public Method getInMethod() {
				return node.getInMethod();
			}

			@Override
			public Boolean isInMethodChained() {
				return node.isInMethodChained();
			}

			@Override
			public Class<?> getPreInputClass() {
				return node.getPreInputClass();
			}

			@Override
			public Class<?> getPostInputClass() {
				return node.getPostInputClass();
			}

			@Override
			public void process(SchemaProcessingContext context) {
				context.accept(this);
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
		};
	}
}
