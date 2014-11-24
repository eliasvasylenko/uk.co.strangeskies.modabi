package uk.co.strangeskies.modabi.schema.node;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
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
		return SchemaNode.super.propertySet().add(BindingNode::getDataType)
				.add(BindingNode::isAbstract).add(BindingNode::getBindingStrategy)
				.add(BindingNode::getBindingType)
				.add(BindingNode::getUnbindingStrategy)
				.add(BindingNode::getUnbindingType)
				.add(BindingNode::getUnbindingMethodName)
				.add(BindingNode::getUnbindingFactoryType)
				.add(BindingNode::getProvidedUnbindingMethodParameterNames);
	}

	Class<T> getDataClass();

	ParameterizedType getDataType();

	BindingStrategy getBindingStrategy();

	ParameterizedType getBindingType();

	UnbindingStrategy getUnbindingStrategy();

	ParameterizedType getUnbindingType();

	String getUnbindingMethodName();

	ParameterizedType getUnbindingFactoryType();

	List<QualifiedName> getProvidedUnbindingMethodParameterNames();
}
