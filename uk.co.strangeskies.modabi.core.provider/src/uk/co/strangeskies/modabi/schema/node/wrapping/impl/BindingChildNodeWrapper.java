/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.core.provider.
 *
 * uk.co.strangeskies.modabi.core.provider is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.core.provider is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.core.provider.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi.schema.node.wrapping.impl;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;

import uk.co.strangeskies.mathematics.Range;
import uk.co.strangeskies.modabi.schema.node.BindingChildNode;
import uk.co.strangeskies.modabi.schema.node.BindingNode;
import uk.co.strangeskies.reflection.TypeToken;

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
	public final Boolean isOutMethodUnchecked() {
		return getBase() == null ? null : getBase().isOutMethodUnchecked();
	}

	@Override
	public final Boolean isOutMethodCast() {
		return getBase() == null ? null : getBase().isOutMethodCast();
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
	public final Boolean isInMethodUnchecked() {
		return getBase() == null ? null : getBase().isInMethodUnchecked();
	}

	@Override
	public final TypeToken<?> getPreInputType() {
		return getBase() == null ? null : getBase().getPreInputType();
	}

	@Override
	public final TypeToken<?> getPostInputType() {
		return getBase() == null ? null : getBase().getPostInputType();
	}
}
