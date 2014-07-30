package uk.co.strangeskies.modabi.model.nodes;

import java.lang.reflect.Method;

import uk.co.strangeskies.modabi.model.building.configurators.BindingNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.configurators.impl.BindingNodeConfiguratorImpl;
import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.processing.BindingStrategy;
import uk.co.strangeskies.modabi.schema.processing.UnbindingStrategy;

public interface BindingNode<T, E extends BindingNode.Effective<T, E>> extends
		SchemaNode<E> {
	interface Effective<T, E extends Effective<T, E>> extends BindingNode<T, E>,
			SchemaNode.Effective<E> {
		Method getUnbindingMethod();

		public static Method findUnbindingMethod(
				BindingNode.Effective<?, ?> effective) {
			if (effective.getDataClass() == null)
				return null;

			UnbindingStrategy unbindingStrategy = effective.getUnbindingStrategy();
			if (unbindingStrategy == null)
				unbindingStrategy = UnbindingStrategy.SIMPLE;

			Class<?> receiverClass = null;
			Class<?> resultClass = null;
			Class<?>[] parameters = new Class<?>[0];

			switch (unbindingStrategy) {
			case SIMPLE:
				if (effective.getUnbindingClass() != null)
					throw new SchemaException();
			case CONSTRUCTOR:
				if (effective.getUnbindingMethodName() != null)
					throw new SchemaException();
				return null;
			case STATIC_FACTORY:
			case PROVIDED_FACTORY:
				receiverClass = effective.getUnbindingFactoryClass() != null ? effective
						.getUnbindingFactoryClass()
						: effective.getUnbindingClass() != null ? effective
								.getUnbindingClass() : effective.getDataClass();
				parameters = new Class<?>[] { effective.getDataClass() };
				resultClass = effective.getUnbindingClass();
				break;
			case PASS_TO_PROVIDED:
				receiverClass = effective.getUnbindingClass();
				parameters = new Class<?>[] { effective.getDataClass() };
				break;
			case ACCEPT_PROVIDED:
				receiverClass = effective.getDataClass();
				if (effective.getUnbindingClass() == null)
					throw new SchemaException();
				parameters = new Class<?>[] { effective.getUnbindingClass() };
			}
			Method unbindingMethod = null;
			try {
				unbindingMethod = BindingNodeConfigurator.findMethod(
						BindingNodeConfiguratorImpl.getNames(effective.getId(),
								effective.getUnbindingMethodName(), resultClass),
						receiverClass, resultClass, parameters);
			} catch (NoSuchMethodException | SecurityException e) {
				throw new SchemaException(e);
			}
			return unbindingMethod;
		}
	}

	Class<T> getDataClass();

	BindingStrategy getBindingStrategy();

	Class<?> getBindingClass();

	UnbindingStrategy getUnbindingStrategy();

	Class<?> getUnbindingClass();

	String getUnbindingMethodName();

	Class<?> getUnbindingFactoryClass();
}
