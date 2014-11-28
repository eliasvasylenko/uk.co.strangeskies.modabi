package uk.co.strangeskies.modabi.schema.node.wrapping.impl;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import uk.co.strangeskies.mathematics.Range;
import uk.co.strangeskies.modabi.schema.node.BindingChildNode;
import uk.co.strangeskies.modabi.schema.node.BindingNode;

public abstract class BindingChildNodeWrapper<T, C extends BindingNode.Effective<? super T, ?, ?>, B extends BindingChildNode.Effective<? super T, ?, ?>, S extends BindingChildNode<T, S, E>, E extends BindingChildNode.Effective<T, S, E>>
		extends BindingNodeWrapper<T, C, B, S, E> implements
		BindingChildNode.Effective<T, S, E> {
	public BindingChildNodeWrapper(C component) {
		super(component);
	}

	public BindingChildNodeWrapper(C component, B base) {
		super(component, base);
	}

	@Override
	public final Boolean isOrdered() {
		return getBase() == null ? null : getBase().isOrdered();
	}

	@Override
	public final Boolean isExtensible() {
		return getBase() == null ? null : getBase().isExtensible();
	}

	@Override
	public final Method getOutMethod() {
		return getBase() == null ? null : getBase().getOutMethod();
	}

	@Override
	public final String getOutMethodName() {
		return getBase() == null ? null : getBase().getOutMethodName();
	}

	@Override
	public final Boolean isOutMethodIterable() {
		return getBase() == null ? null : getBase().isOutMethodIterable();
	}

	@Override
	public final Range<Integer> occurrences() {
		return getBase() == null ? null : getBase().occurrences();
	}

	@Override
	public Boolean isInMethodCast() {
		return getBase() == null ? null : getBase().isInMethodCast();
	}

	@Override
	public final String getInMethodName() {
		return getBase() == null ? null : getBase().getInMethodName();
	}

	@Override
	public final Executable getInMethod() {
		return getBase() == null ? null : getBase().getInMethod();
	}

	@Override
	public final Boolean isInMethodChained() {
		return getBase() == null ? null : getBase().isInMethodChained();
	}

	@Override
	public final Type getPreInputType() {
		return getBase() == null ? null : getBase().getPreInputType();
	}

	@Override
	public final Type getPostInputType() {
		return getBase() == null ? null : getBase().getPostInputType();
	}
}
