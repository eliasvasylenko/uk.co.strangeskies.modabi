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
import uk.co.strangeskies.modabi.impl.schema.utilities.OverrideMerge;
import uk.co.strangeskies.modabi.schema.InputSequenceNode;
import uk.co.strangeskies.reflection.Invokable;
import uk.co.strangeskies.reflection.TypeToken;

class InputSequenceNodeImpl extends ChildNodeImpl<InputSequenceNode, InputSequenceNode.Effective>
		implements InputSequenceNode {
	private static class Effective extends ChildNodeImpl.Effective<InputSequenceNode, InputSequenceNode.Effective>
			implements InputSequenceNode.Effective {
		private final String inMethodName;
		private final Invokable<?, ?> inMethod;
		private final Boolean inMethodChained;
		private final Boolean allowInMethodResultCast;
		private final Boolean inMethodUnchecked;

		private final TypeToken<?> preInputClass;
		private final TypeToken<?> postInputClass;

		protected Effective(OverrideMerge<InputSequenceNode, InputSequenceNodeConfiguratorImpl> overrideMerge) {
			super(overrideMerge);

			if (!overrideMerge.configurator().getContext().isInputExpected())
				throw new ModabiException(t -> t.cannotDefineInputInContext(name()));

			List<TypeToken<?>> parameterClasses = overrideMerge.configurator().getChildrenContainer().getChildren().stream()
					.map(o -> ((BindingChildNodeImpl.Effective<?, ?, ?>) o.effective()).dataType()).collect(Collectors.toList());

			InputNodeConfigurationHelper<InputSequenceNode, InputSequenceNode.Effective> inputNodeHelper = new InputNodeConfigurationHelper<>(
					abstractness(), name(), overrideMerge, overrideMerge.configurator().getContext(), parameterClasses);

			inMethodChained = inputNodeHelper.isInMethodChained();
			allowInMethodResultCast = inputNodeHelper.isInMethodCast();
			inMethodUnchecked = inputNodeHelper.isInMethodUnchecked();
			inMethod = inputNodeHelper.getInMethod();
			inMethodName = inputNodeHelper.getInMethodName();
			preInputClass = inputNodeHelper.getPreInputType();
			postInputClass = inputNodeHelper.getPostInputType();
		}

		@Override
		public final String inMethodName() {
			return inMethodName;
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

	private final InputSequenceNodeImpl.Effective effective;

	private final TypeToken<?> postInputClass;
	private final String inMethodName;
	private final Boolean inMethodChained;
	private final Boolean allowInMethodResultCast;
	private final Boolean inMethodUnchecked;

	public InputSequenceNodeImpl(InputSequenceNodeConfiguratorImpl configurator) {
		super(configurator);

		postInputClass = configurator.getPostInputClass();
		inMethodName = configurator.getInMethodName();
		inMethodChained = configurator.getInMethodChained();
		allowInMethodResultCast = configurator.getInMethodCast();
		inMethodUnchecked = configurator.getInMethodUnchecked();

		effective = new Effective(InputSequenceNodeConfiguratorImpl.overrideMerge(this, configurator));
	}

	@Override
	public final String inMethodName() {
		return inMethodName;
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
	public InputSequenceNodeImpl.Effective effective() {
		return effective;
	}

	@Override
	public TypeToken<?> postInputType() {
		return postInputClass;
	}
}
