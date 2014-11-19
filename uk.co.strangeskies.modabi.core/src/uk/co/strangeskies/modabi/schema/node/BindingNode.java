package uk.co.strangeskies.modabi.schema.node;

import java.lang.reflect.Method;
import java.util.List;

import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.management.binding.BindingStrategy;
import uk.co.strangeskies.modabi.schema.management.unbinding.UnbindingStrategy;
import uk.co.strangeskies.utilities.PropertySet;

public interface BindingNode<T, S extends BindingNode<T, S, E>, E extends BindingNode.Effective<T, S, E>>
		extends SchemaNode<S, E> {
	interface Effective<T, S extends BindingNode<T, S, E>, E extends Effective<T, S, E>>
			extends BindingNode<T, S, E>, SchemaNode.Effective<S, E> {
		Method getUnbindingMethod();

		List<DataNode.Effective<?>> getProvidedUnbindingMethodParameters();

		@Override
		default PropertySet<E> effectivePropertySet() {
			return SchemaNode.Effective.super.effectivePropertySet()
					.add(BindingNode.Effective::getUnbindingMethod)
					.add(BindingNode.Effective::getProvidedUnbindingMethodParameters);
		}
	}

	@Override
	default PropertySet<S> propertySet() {
		return SchemaNode.super.propertySet().add(BindingNode::getDataClass)
				.add(BindingNode::isAbstract).add(BindingNode::getBindingStrategy)
				.add(BindingNode::getBindingClass)
				.add(BindingNode::getUnbindingStrategy)
				.add(BindingNode::getUnbindingClass)
				.add(BindingNode::getUnbindingMethodName)
				.add(BindingNode::getUnbindingFactoryClass)
				.add(BindingNode::getProvidedUnbindingMethodParameterNames);
	}

	Class<T> getDataClass();

	BindingStrategy getBindingStrategy();

	Class<?> getBindingClass();

	UnbindingStrategy getUnbindingStrategy();

	Class<?> getUnbindingClass();

	String getUnbindingMethodName();

	Class<?> getUnbindingFactoryClass();

	List<QualifiedName> getProvidedUnbindingMethodParameterNames();
}
