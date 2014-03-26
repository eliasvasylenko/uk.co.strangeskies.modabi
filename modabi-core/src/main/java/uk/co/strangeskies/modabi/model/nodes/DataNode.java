package uk.co.strangeskies.modabi.model.nodes;

import java.lang.reflect.Method;
import java.util.List;

import uk.co.strangeskies.gears.mathematics.Range;
import uk.co.strangeskies.modabi.data.DataType;
import uk.co.strangeskies.modabi.processing.BindingStrategy;
import uk.co.strangeskies.modabi.processing.UnbindingStrategy;

public interface DataNode<T> extends BindingChildNode<T> {
	public enum Format {
		PROPERTY, CONTENT, SIMPLE_ELEMENT
	}

	public Format format();

	public DataType<T> type();

	public default boolean isValueSet() {
		return value() != null;
	}

	public T value();

	public Range<Integer> occurances();

	public Boolean isOptional();

	@Override
	public default BindingStrategy getBindingStrategy() {
		return type().getBindingStrategy();
	}

	@Override
	public default Class<?> getBindingClass() {
		return type().getBindingClass();
	}

	@Override
	public default UnbindingStrategy getUnbindingStrategy() {
		return type().getUnbindingStrategy();
	}

	@Override
	public default Class<?> getUnbindingClass() {
		return type().getUnbindingClass();
	}

	@Override
	public default Method getUnbindingMethod() {
		return type().getUnbindingMethod();
	}

	@Override
	public default List<? extends ChildNode> getChildren() {
		return type().getChildren();
	}
}
