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
package uk.co.strangeskies.modabi.impl.schema;

import java.lang.reflect.Executable;
import java.util.List;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.impl.schema.utilities.OverrideMerge;
import uk.co.strangeskies.modabi.schema.InputSequenceNode;
import uk.co.strangeskies.reflection.TypeToken;

class InputSequenceNodeImpl
		extends SchemaNodeImpl<InputSequenceNode, InputSequenceNode.Effective>
		implements InputSequenceNode {
	private static class Effective extends
			SchemaNodeImpl.Effective<InputSequenceNode, InputSequenceNode.Effective>
			implements InputSequenceNode.Effective {
		private final String inMethodName;
		private final Executable inMethod;
		private final Boolean inMethodChained;
		private final Boolean allowInMethodResultCast;
		private final Boolean inMethodUnchecked;

		private final TypeToken<?> preInputClass;
		private final TypeToken<?> postInputClass;

		protected Effective(
				OverrideMerge<InputSequenceNode, InputSequenceNodeConfiguratorImpl> overrideMerge) {
			super(overrideMerge);

			if (!overrideMerge.configurator().getContext().isInputExpected())
				throw new SchemaException("InputSequenceNode '" + getName()
						+ "' cannot occur in a context without input.");

			List<TypeToken<?>> parameterClasses = overrideMerge.configurator()
					.getChildrenContainer().getChildren().stream()
					.map(o -> ((BindingChildNodeImpl.Effective<?, ?, ?>) o.effective())
							.getDataType())
					.collect(Collectors.toList());

			InputNodeConfigurationHelper<InputSequenceNode, InputSequenceNode.Effective> inputNodeHelper = new InputNodeConfigurationHelper<>(
					isAbstract(), getName(), overrideMerge,
					overrideMerge.configurator().getContext(), parameterClasses);

			inMethodChained = inputNodeHelper.isInMethodChained();
			allowInMethodResultCast = inputNodeHelper.isInMethodCast();
			inMethodUnchecked = inputNodeHelper.isInMethodUnchecked();
			inMethod = inputNodeHelper.getInMethod() != null
					? inputNodeHelper.getInMethod().getExecutable() : null;
			inMethodName = inputNodeHelper.getInMethodName();
			preInputClass = inputNodeHelper.getPreInputType();
			postInputClass = inputNodeHelper.getPostInputType();
		}

		@Override
		public final String getInMethodName() {
			return inMethodName;
		}

		@Override
		public Executable getInMethod() {
			return inMethod;
		}

		@Override
		public final Boolean isInMethodChained() {
			return inMethodChained;
		}

		@Override
		public Boolean isInMethodCast() {
			return allowInMethodResultCast;
		}

		@Override
		public Boolean isInMethodUnchecked() {
			return inMethodUnchecked;
		}

		@Override
		public TypeToken<?> getPostInputType() {
			return postInputClass;
		}

		@Override
		public TypeToken<?> getPreInputType() {
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

		effective = new Effective(
				InputSequenceNodeConfiguratorImpl.overrideMerge(this, configurator));
	}

	@Override
	public final String getInMethodName() {
		return inMethodName;
	}

	@Override
	public final Boolean isInMethodChained() {
		return inMethodChained;
	}

	@Override
	public Boolean isInMethodCast() {
		return allowInMethodResultCast;
	}

	@Override
	public Boolean isInMethodUnchecked() {
		return inMethodUnchecked;
	}

	@Override
	public InputSequenceNodeImpl.Effective effective() {
		return effective;
	}

	@Override
	public TypeToken<?> getPostInputType() {
		return postInputClass;
	}
}
