package uk.co.strangeskies.modabi.model.nodes;

import java.lang.reflect.Method;
import java.util.List;

import uk.co.strangeskies.modabi.data.BufferedDataSource;
import uk.co.strangeskies.modabi.data.DataType;
import uk.co.strangeskies.modabi.processing.BindingStrategy;
import uk.co.strangeskies.modabi.processing.SchemaProcessingContext;
import uk.co.strangeskies.modabi.processing.UnbindingStrategy;

public interface DataNode<T> extends BindingChildNode<T> {
	public enum Format {
		PROPERTY, CONTENT, SIMPLE_ELEMENT
	}

	Format format();

	DataType<T> type();

	default boolean isValueSet() {
		return value() != null;
	}

	BufferedDataSource value();

	Boolean optional();

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
	default List<? extends ChildNode> getChildren() {
		return type().getChildren();
	}

	@Override
	public default void process(SchemaProcessingContext context) {
		context.accept(this);
	}
}
