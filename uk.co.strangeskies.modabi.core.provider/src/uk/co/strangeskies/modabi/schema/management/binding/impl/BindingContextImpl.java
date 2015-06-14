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
package uk.co.strangeskies.modabi.schema.management.binding.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import uk.co.strangeskies.modabi.io.structured.StructuredDataSource;
import uk.co.strangeskies.modabi.schema.management.SchemaManager;
import uk.co.strangeskies.modabi.schema.management.binding.BindingContext;
import uk.co.strangeskies.modabi.schema.management.binding.BindingException;
import uk.co.strangeskies.modabi.schema.management.impl.ProcessingContextImpl;
import uk.co.strangeskies.modabi.schema.node.SchemaNode;
import uk.co.strangeskies.reflection.TypeToken;

public class BindingContextImpl extends
		ProcessingContextImpl<BindingContextImpl> implements BindingContext {
	private final List<Object> bindingTargetStack;
	private final StructuredDataSource input;

	public BindingContextImpl(SchemaManager manager) {
		super(manager);

		this.bindingTargetStack = Collections.emptyList();
		this.input = null;
	}

	private BindingContextImpl(BindingContextImpl parent,
			List<Object> bindingTargetStack, StructuredDataSource input,
			ProcessingProvisions provider) {
		super(parent, provider);
		this.bindingTargetStack = bindingTargetStack;
		this.input = input;
	}

	private BindingContextImpl(BindingContextImpl parent,
			SchemaNode.Effective<?, ?> node) {
		super(parent, node);
		this.bindingTargetStack = parent.bindingTargetStack;
		this.input = parent.input;
	}

	@Override
	public StructuredDataSource input() {
		return input;
	}

	@Override
	public List<Object> bindingTargetStack() {
		return bindingTargetStack;
	}

	@Override
	protected RuntimeException processingException(String message,
			BindingContextImpl state) {
		throw new BindingException(message, state);
	}

	@Override
	public <T> BindingContextImpl withProvision(TypeToken<T> providedClass,
			Function<? super BindingContextImpl, T> provider,
			ProcessingContextImpl<BindingContextImpl>.ProcessingProvisions provisions) {
		return new BindingContextImpl(this, bindingTargetStack, input, provisions);
	}

	public <T> BindingContextImpl withBindingTarget(Object target) {
		List<Object> bindingTargetStack = new ArrayList<>(bindingTargetStack());
		bindingTargetStack.add(target);

		return new BindingContextImpl(this,
				Collections.unmodifiableList(bindingTargetStack), input, getProvider());
	}

	public <T> BindingContextImpl withBindingNode(SchemaNode.Effective<?, ?> node) {
		return new BindingContextImpl(this, node);
	}

	public BindingContextImpl withInput(StructuredDataSource input) {
		return new BindingContextImpl(this, bindingTargetStack, input,
				getProvider());
	}

	public void attempt(Consumer<BindingContextImpl> bindingMethod) {
		bindingMethod.accept(this);
	}

	public <U> U attempt(Function<BindingContextImpl, U> bindingMethod) {
		return bindingMethod.apply(this);
	}

	public <I> I attemptUntilSuccessful(Iterable<I> attemptItems,
			BiConsumer<BindingContextImpl, I> bindingMethod,
			Function<Set<Exception>, BindingException> onFailure) {
		throw new BindingException("attemptUntilSuccessful unimplemented.", this);
	}
}
