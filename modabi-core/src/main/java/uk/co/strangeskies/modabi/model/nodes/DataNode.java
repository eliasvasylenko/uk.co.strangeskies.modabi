package uk.co.strangeskies.modabi.model.nodes;

import java.lang.reflect.Method;
import java.util.List;

import uk.co.strangeskies.gears.mathematics.Range;
import uk.co.strangeskies.modabi.data.DataBindingType;
import uk.co.strangeskies.modabi.data.io.BufferedDataSource;
import uk.co.strangeskies.modabi.schema.processing.BindingStrategy;
import uk.co.strangeskies.modabi.schema.processing.SchemaProcessingContext;
import uk.co.strangeskies.modabi.schema.processing.UnbindingStrategy;

public interface DataNode<T> extends BindingChildNode<T>, DataNodeChildNode {
	public enum Format {
		PROPERTY, CONTENT, SIMPLE_ELEMENT
	}

	Format format();

	DataBindingType<T> type();

	default boolean isValueSet() {
		return value() != null;
	}

	BufferedDataSource value();

	Boolean optional();

	@Override
	public default Class<T> getDataClass() {
		return type().getDataClass();
	}

	@Override
	default BindingStrategy getBindingStrategy() {
		return type().getBindingStrategy();
	}

	@Override
	default Class<?> getBindingClass() {
		return type().getBindingClass();
	}

	@Override
	default UnbindingStrategy getUnbindingStrategy() {
		return type().getUnbindingStrategy();
	}

	@Override
	default Class<?> getUnbindingClass() {
		return type().getUnbindingClass();
	}

	@Override
	default Method getUnbindingMethod() {
		return type().getUnbindingMethod();
	}

	@Override
	public default void process(SchemaProcessingContext context) {
		context.accept(this);
	}

	public static <T> DataNode<T> wrapType(DataNode<T> node) {
		if (node.type() == null)
			return node;

		return new DataNode<T>() {
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
				return node.type().getDataClass();
			}

			@Override
			public BindingStrategy getBindingStrategy() {
				return node.type().getBindingStrategy();
			}

			@Override
			public Class<?> getBindingClass() {
				return node.type().getBindingClass();
			}

			@Override
			public UnbindingStrategy getUnbindingStrategy() {
				return node.type().getUnbindingStrategy();
			}

			@Override
			public Class<?> getUnbindingClass() {
				return node.type().getUnbindingClass();
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
			public BufferedDataSource value() {
				return node.value();
			}

			@Override
			public Boolean optional() {
				return node.optional();
			}
		};
	}
}
