package uk.co.strangeskies.modabi.model.nodes;

import java.lang.reflect.Method;
import java.util.Objects;

import uk.co.strangeskies.gears.utilities.PropertySet;

public interface InputNode<S extends InputNode<S, E>, E extends InputNode.Effective<S, E>>
		extends ChildNode<S, E> {
	interface Effective<S extends InputNode<S, E>, E extends Effective<S, E>>
			extends InputNode<S, E>, ChildNode.Effective<S, E> {
		@Override
		default Class<?> getPreInputClass() {
			return getInMethod() == null ? null : getInMethod().getDeclaringClass();
		}

		@Override
		default Class<?> getPostInputClass() {
			return (isInMethodChained() == null || !isInMethodChained() || Objects
					.equals(getInMethodName(), "null")) ? getPreInputClass()
					: (getInMethod() == null ? null : getInMethod().getReturnType());
		}

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
				.add(InputNode::isInMethodChained);
	}

	String getInMethodName();

	/**
	 * If this method returns true, the return value of any invocation of the
	 * inMethod will replace the build class of any
	 *
	 * @return
	 */
	Boolean isInMethodChained();
}
