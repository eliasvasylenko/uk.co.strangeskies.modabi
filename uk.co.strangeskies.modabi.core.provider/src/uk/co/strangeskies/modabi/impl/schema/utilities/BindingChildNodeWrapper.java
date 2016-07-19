/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
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
package uk.co.strangeskies.modabi.impl.schema.utilities;

import uk.co.strangeskies.mathematics.Range;
import uk.co.strangeskies.modabi.schema.BindingChildNode;
import uk.co.strangeskies.modabi.schema.BindingNode;
import uk.co.strangeskies.reflection.Invokable;
import uk.co.strangeskies.reflection.TypeToken;

public abstract class BindingChildNodeWrapper<T, B extends BindingChildNode<? super T, B>, S extends BindingChildNode<T, S>>
		extends BindingNodeWrapper<T, B, S> implements BindingChildNode<T, S> {
	public BindingChildNodeWrapper(BindingNode<T, ?> component) {
		super(component);
	}

	public BindingChildNodeWrapper(B base, BindingNode<?, ?> component) {
		super(base, component);
	}

	@Override
	public final Boolean ordered() {
		return getBase() == null ? null : getBase().ordered();
	}

	@Override
	public final Boolean extensible() {
		return getBase() == null ? null : getBase().extensible();
	}

	@Override
	public Boolean synchronous() {
		return getBase() == null ? null : getBase().synchronous();
	}

	@Override
	public final Invokable<?, ?> outMethod() {
		return getBase() == null ? null : getBase().outMethod();
	}

	@Override
	public final String outMethodName() {
		return getBase() == null ? null : getBase().outMethodName();
	}

	@Override
	public final Boolean outMethodIterable() {
		return getBase() == null ? null : getBase().outMethodIterable();
	}

	@Override
	public final Boolean outMethodUnchecked() {
		return getBase() == null ? null : getBase().outMethodUnchecked();
	}

	@Override
	public final Boolean outMethodCast() {
		return getBase() == null ? null : getBase().outMethodCast();
	}

	@Override
	public final Range<Integer> occurrences() {
		return getBase() == null ? null : getBase().occurrences();
	}

	@Override
	public Boolean inMethodCast() {
		return getBase() == null ? null : getBase().inMethodCast();
	}

	@Override
	public final String inMethodName() {
		return getBase() == null ? null : getBase().inMethodName();
	}

	@Override
	public final Invokable<?, ?> inMethod() {
		return getBase() == null ? null : getBase().inMethod();
	}

	@Override
	public final Boolean inMethodChained() {
		return getBase() == null ? null : getBase().inMethodChained();
	}

	@Override
	public final Boolean inMethodUnchecked() {
		return getBase() == null ? null : getBase().inMethodUnchecked();
	}

	@Override
	public final TypeToken<?> preInputType() {
		return getBase() == null ? null : getBase().preInputType();
	}

	@Override
	public final TypeToken<?> postInputType() {
		return getBase() == null ? null : getBase().postInputType();
	}

	@Override
	public Boolean nullIfOmitted() {
		return getBase() == null ? null : getBase().nullIfOmitted();
	}
}
