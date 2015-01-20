package uk.co.strangeskies.modabi.schema.node;

import java.lang.reflect.Executable;
import java.lang.reflect.Type;
import java.util.List;

import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.management.binding.BindingStrategy;
import uk.co.strangeskies.modabi.schema.management.unbinding.UnbindingStrategy;
import uk.co.strangeskies.reflection.TypeLiteral;
import uk.co.strangeskies.utilities.PropertySet;

public interface BindingNode<T, S extends BindingNode<T, S, E>, E extends BindingNode.Effective<T, S, E>>
		extends SchemaNode<S, E> {
	interface Effective<T, S extends BindingNode<T, S, E>, E extends Effective<T, S, E>>
			extends BindingNode<T, S, E>, SchemaNode.Effective<S, E> {
		Executable getUnbindingMethod();

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

	TypeLiteral<T> getDataType();

	BindingStrategy getBindingStrategy();

	Type getBindingType();

	UnbindingStrategy getUnbindingStrategy();

	Type getUnbindingType();

	String getUnbindingMethodName();

	Type getUnbindingFactoryType();

	List<QualifiedName> getProvidedUnbindingMethodParameterNames();
}
