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
import uk.co.strangeskies.reflection.ExecutableMember;
import uk.co.strangeskies.reflection.FieldMember;
import uk.co.strangeskies.reflection.TypeToken;

class InputSequenceNodeImpl extends ChildNodeImpl<InputSequenceNode> implements InputSequenceNode {
	private final InputNodeComponent<?, ?> inputNodeComponent;

	protected InputSequenceNodeImpl(InputSequenceNodeConfiguratorImpl configurator) {
		super(configurator);

		if (!configurator.getContext().isInputExpected())
			throw new ModabiException(t -> t.cannotDefineInputInContext(name()));

		List<TypeToken<?>> parameterClasses = configurator.getChildrenResults().stream()
				.map(o -> ((BindingChildNodeImpl<?, ?>) o).dataType()).collect(Collectors.toList());

		inputNodeComponent = new InputNodeComponent<>(configurator, parameterClasses);
	}

	protected InputNodeComponent<?, ?> getInputNodeComponent() {
		return inputNodeComponent;
	}

	@Override
	public InputSequenceNodeConfigurator configurator() {
		return (InputSequenceNodeConfigurator) super.configurator();
	}

	@Override
	public InputMemberType inputMemberType() {
		return getInputNodeComponent().getInputMemberType();
	}

	@Override
	public final ExecutableMember<?, ?> inputExecutable() {
		return inputMemberType() != InputMemberType.FIELD
				? (ExecutableMember<?, ?>) getInputNodeComponent().getInputMember() : null;
	}

	@Override
	public FieldMember<?, ?> inputField() {
		return inputMemberType() == InputMemberType.FIELD ? (FieldMember<?, ?>) getInputNodeComponent().getInputMember()
				: null;
	}

	@Override
	public final Boolean chainedInput() {
		return getInputNodeComponent().isInMethodChained();
	}

	@Override
	public Boolean castInput() {
		return getInputNodeComponent().isInMethodCast();
	}

	@Override
	public Boolean uncheckedInput() {
		return getInputNodeComponent().isInMethodUnchecked();
	}

	@Override
	public TypeToken<?> postInputType() {
		return getInputNodeComponent().getPostInputType();
	}

	@Override
	public TypeToken<?> preInputType() {
		return getInputNodeComponent().getPreInputType();
	}
}
