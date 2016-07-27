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
package uk.co.strangeskies.modabi.impl.schema;

import java.util.List;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.ModabiException;
import uk.co.strangeskies.modabi.schema.InputSequenceNode;
import uk.co.strangeskies.modabi.schema.InputSequenceNodeConfigurator;
import uk.co.strangeskies.reflection.Invokable;
import uk.co.strangeskies.reflection.TypeToken;

class InputSequenceNodeImpl extends ChildNodeImpl<InputSequenceNode> implements InputSequenceNode {
	private final Invokable<?, ?> inMethod;
	private final Boolean inMethodChained;
	private final Boolean allowInMethodResultCast;
	private final Boolean inMethodUnchecked;

	private final TypeToken<?> preInputClass;
	private final TypeToken<?> postInputClass;

	protected InputSequenceNodeImpl(InputSequenceNodeConfiguratorImpl configurator) {
		super(configurator);

		if (!configurator.getContext().isInputExpected())
			throw new ModabiException(t -> t.cannotDefineInputInContext(name()));

		List<TypeToken<?>> parameterClasses = configurator.getChildren().stream()
				.map(o -> ((BindingChildNodeImpl<?, ?>) o).dataType()).collect(Collectors.toList());

		InputNodeConfigurationHelper<InputSequenceNode> inputNodeHelper = new InputNodeConfigurationHelper<>(abstractness(),
				name(), configurator, configurator.getContext(), parameterClasses);

		inMethodChained = inputNodeHelper.isInMethodChained();
		allowInMethodResultCast = inputNodeHelper.isInMethodCast();
		inMethodUnchecked = inputNodeHelper.isInMethodUnchecked();
		inMethod = inputNodeHelper.getInMethod();
		preInputClass = inputNodeHelper.getPreInputType();
		postInputClass = inputNodeHelper.getPostInputType();
	}

	@Override
	public InputSequenceNodeConfigurator configurator() {
		return (InputSequenceNodeConfigurator) super.configurator();
	}

	@Override
	public Invokable<?, ?> inMethod() {
		return inMethod;
	}

	@Override
	public final Boolean inMethodChained() {
		return inMethodChained;
	}

	@Override
	public Boolean inMethodCast() {
		return allowInMethodResultCast;
	}

	@Override
	public Boolean inMethodUnchecked() {
		return inMethodUnchecked;
	}

	@Override
	public TypeToken<?> postInputType() {
		return postInputClass;
	}

	@Override
	public TypeToken<?> preInputType() {
		return preInputClass;
	}
}
