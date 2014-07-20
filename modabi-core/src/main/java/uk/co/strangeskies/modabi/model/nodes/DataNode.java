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

	public interface Value<T> {
		public enum ValueResolution {
			REGISTRATION_TIME, PROCESSING_TIME;
		}

		default boolean isValuePresent() {
			return (resolution() == ValueResolution.PROCESSING_TIME && providedBuffer() != null)
					|| (resolution() == ValueResolution.REGISTRATION_TIME && provided() != null);
		}

		BufferedDataSource providedBuffer();

		T provided();

		ValueResolution resolution();
	}

	Format format();

	Value<T> value();

	DataBindingType<T> type();

	Boolean optional();

	@Override
	default Class<T> getDataClass() {
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
	default void process(SchemaProcessingContext context) {
		context.accept(this);
	}

	static <T> DataNode<T> wrapType(DataNode<T> node) {
		if (node.type() == null)
			return node;

		return new DataNode<T>() {
			private final Value<T> value = new Value<T>() {
				@Override
				public BufferedDataSource providedBuffer() {
					return node.value().providedBuffer();
				}

				@Override
				public T provided() {
					return node.value().provided();
				}

				@Override
				public ValueResolution resolution() {
					return node.value().resolution();
				}
			};

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
			public Value<T> value() {
				return value;
			}

			@Override
			public Boolean optional() {
				return node.optional();
			}
		};
	}
}
