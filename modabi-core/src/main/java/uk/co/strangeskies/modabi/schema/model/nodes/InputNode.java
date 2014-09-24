package uk.co.strangeskies.modabi.schema.model.nodes;

import java.lang.reflect.Method;

import uk.co.strangeskies.utilities.PropertySet;

public interface InputNode<S extends InputNode<S, E>, E extends InputNode.Effective<S, E>>
		extends ChildNode<S, E> {
	interface Effective<S extends InputNode<S, E>, E extends Effective<S, E>>
			extends InputNode<S, E>, ChildNode.Effective<S, E> {
		Method getInMethod();

		@Override
		default PropertySet<E> effectivePropertySet() {
			return ChildNode.Effective.super.effectivePropertySet().add(
					InputNode.Effective::getInMethod);
		}
	}

	@Override
	default PropertySet<S> propertySet() {
		return ChildNode.super.propertySet().add(InputNode::getInMethodName)
				.add(InputNode::isInMethodChained).add(InputNode::isInMethodCast);
	}

	String getInMethodName();

	/**
	 * If this method returns true, the return value of any invocation of the
	 * inMethod will replace the build class of any
	 *
	 * @return
	 */
	Boolean isInMethodChained();

	Boolean isInMethodCast();
}
