package uk.co.strangeskies.modabi.data;

import java.lang.reflect.Method;
import java.util.List;

import uk.co.strangeskies.modabi.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.schema.processing.BindingStrategy;
import uk.co.strangeskies.modabi.schema.processing.UnbindingStrategy;

public interface DataBindingType<T> {
	interface Effective<T> extends DataBindingType<T> {
		@Override
		default Effective<T> effective() {
			return this;
		}

		Method getUnbindingMethod();

		@Override
		DataBindingType.Effective<? super T> baseType();

		@Override
		List<ChildNode.Effective<?>> getChildren();
	}

	String getName();

	Class<T> getDataClass();

	Class<?> getBindingClass();

	BindingStrategy getBindingStrategy();

	Class<?> getUnbindingClass();

	Class<?> getUnbindingFactoryClass();

	UnbindingStrategy getUnbindingStrategy();

	String getUnbindingMethodName();

	Boolean isAbstract();

	Boolean isPrivate();

	List<? extends ChildNode<?>> getChildren();

	DataBindingType<? super T> baseType();

	Effective<T> effective();
}
