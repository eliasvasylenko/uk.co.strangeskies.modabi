package uk.co.strangeskies.modabi.model.nodes;

import java.lang.reflect.Method;

import uk.co.strangeskies.mathematics.Range;
import uk.co.strangeskies.utilities.PropertySet;

public interface BindingChildNode<T, S extends BindingChildNode<T, S, E>, E extends BindingChildNode.Effective<T, S, E>>
		extends BindingNode<T, S, E>, InputNode<S, E> {
	interface Effective<T, S extends BindingChildNode<T, S, E>, E extends Effective<T, S, E>>
			extends BindingChildNode<T, S, E>, BindingNode.Effective<T, S, E>,
			InputNode.Effective<S, E> {
		Method getOutMethod();

		@Override
		default PropertySet<E> effectivePropertySet() {
			return BindingNode.Effective.super.effectivePropertySet()
					.add(BindingChildNode.Effective::getOutMethod)
					.add(InputNode.Effective::getInMethod);
		}
	}

	@Override
	default PropertySet<S> propertySet() {
		return BindingNode.super.propertySet()
				.add(BindingChildNode::getOutMethodName)
				.add(BindingChildNode::isOutMethodIterable)
				.add(BindingChildNode::occurances).add(InputNode::getInMethodName)
				.add(InputNode::isInMethodChained).add(BindingChildNode::isExtensible);
	}

	String getOutMethodName();

	/**
	 * If this method returns true, the return value of any invocation of the
	 * inMethod will replace the build class of any
	 *
	 * @return
	 */
	Boolean isOutMethodIterable();

	/**
	 * Default behaviour is as if 1..1.
	 *
	 * @return
	 */
	Range<Integer> occurances();

	/**
	 * Default behaviour is as if true. If unordered, may input concurrently, and
	 * semantics of updating existing binding are more flexible.
	 *
	 * @return
	 */
	Boolean isOrdered();

	Boolean isExtensible();
}
