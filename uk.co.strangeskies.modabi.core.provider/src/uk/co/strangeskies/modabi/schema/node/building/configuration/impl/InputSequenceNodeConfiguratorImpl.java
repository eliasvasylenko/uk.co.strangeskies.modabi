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
package uk.co.strangeskies.modabi.schema.node.building.configuration.impl;

import java.lang.reflect.Executable;
import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.namespace.Namespace;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.node.BindingChildNode;
import uk.co.strangeskies.modabi.schema.node.ChildNode;
import uk.co.strangeskies.modabi.schema.node.InputSequenceNode;
import uk.co.strangeskies.modabi.schema.node.SchemaNode;
import uk.co.strangeskies.modabi.schema.node.building.ChildBuilder;
import uk.co.strangeskies.modabi.schema.node.building.DataLoader;
import uk.co.strangeskies.modabi.schema.node.building.configuration.ChoiceNodeConfigurator;
import uk.co.strangeskies.modabi.schema.node.building.configuration.ComplexNodeConfigurator;
import uk.co.strangeskies.modabi.schema.node.building.configuration.DataNodeConfigurator;
import uk.co.strangeskies.modabi.schema.node.building.configuration.InputSequenceNodeConfigurator;
import uk.co.strangeskies.modabi.schema.node.building.configuration.SequenceNodeConfigurator;
import uk.co.strangeskies.modabi.schema.node.building.configuration.impl.utilities.ChildrenConfigurator;
import uk.co.strangeskies.modabi.schema.node.building.configuration.impl.utilities.OverrideMerge;
import uk.co.strangeskies.modabi.schema.node.building.configuration.impl.utilities.SchemaNodeConfigurationContext;
import uk.co.strangeskies.modabi.schema.node.building.configuration.impl.utilities.SequentialChildrenConfigurator;
import uk.co.strangeskies.reflection.BoundSet;
import uk.co.strangeskies.reflection.TypeToken;

public class InputSequenceNodeConfiguratorImpl extends
		ChildNodeConfiguratorImpl<InputSequenceNodeConfigurator, InputSequenceNode>
		implements InputSequenceNodeConfigurator {
	protected static class InputSequenceNodeImpl extends
			SchemaNodeImpl<InputSequenceNode, InputSequenceNode.Effective> implements
			InputSequenceNode {
		private static class Effective
				extends
				SchemaNodeImpl.Effective<InputSequenceNode, InputSequenceNode.Effective>
				implements InputSequenceNode.Effective {
			private final String inMethodName;
			private final Executable inMethod;
			private final Boolean inMethodChained;
			private final Boolean allowInMethodResultCast;

			private final Type preInputClass;
			private final Type postInputClass;

			protected Effective(
					OverrideMerge<InputSequenceNode, InputSequenceNodeConfiguratorImpl> overrideMerge) {
				super(overrideMerge);

				if (!overrideMerge.configurator().getContext().isInputExpected())
					throw new SchemaException("InputSequenceNode '" + getName()
							+ "' cannot occur in a context without input.");

				List<TypeToken<?>> parameterClasses = overrideMerge
						.configurator()
						.getChildrenContainer()
						.getChildren()
						.stream()
						.map(o -> ((BindingChildNode<?, ?, ?>) o).effective().getDataType())
						.collect(Collectors.toList());

				InputNodeConfigurationHelper<InputSequenceNode, InputSequenceNode.Effective> inputNodeHelper = new InputNodeConfigurationHelper<>(
						isAbstract(), getName(), overrideMerge, overrideMerge
								.configurator().getContext(), parameterClasses);

				inMethodChained = inputNodeHelper.isInMethodChained();
				allowInMethodResultCast = inputNodeHelper.isInMethodCast();
				inMethod = inputNodeHelper.getInMethod() != null ? inputNodeHelper
						.getInMethod().getExecutable() : null;
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
			public Type getPostInputType() {
				return postInputClass;
			}

			@Override
			public Type getPreInputType() {
				return preInputClass;
			}
		}

		private final Effective effective;

		private final Type postInputClass;
		private final String inMethodName;
		private final Boolean inMethodChained;
		private final Boolean allowInMethodResultCast;

		public InputSequenceNodeImpl(InputSequenceNodeConfiguratorImpl configurator) {
			super(configurator);

			postInputClass = configurator.getPostInputClass();
			inMethodName = configurator.inMethodName;
			inMethodChained = configurator.inMethodChained;
			allowInMethodResultCast = configurator.allowInMethodResultCast;

			effective = new Effective(overrideMerge(this, configurator));
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
		public Effective effective() {
			return effective;
		}

		@Override
		public Type getPostInputType() {
			return postInputClass;
		}
	}

	private String inMethodName;
	private Boolean inMethodChained;
	private Boolean allowInMethodResultCast;

	public InputSequenceNodeConfiguratorImpl(
			SchemaNodeConfigurationContext<? super InputSequenceNode> parent) {
		super(parent);
	}

	@Override
	public InputSequenceNode tryCreate() {
		return new InputSequenceNodeImpl(this);
	}

	@Override
	public InputSequenceNodeConfigurator inMethod(String methodName) {
		if (!getContext().isInputExpected() && !inMethodName.equals("null"))
			throw new SchemaException(
					"No input method should be specified on this node.");

		assertConfigurable(inMethodName);
		inMethodName = methodName;

		return this;
	}

	@Override
	public InputSequenceNodeConfigurator inMethodChained(boolean chained) {
		assertConfigurable(inMethodChained);
		inMethodChained = chained;

		return this;
	}

	@Override
	public InputSequenceNodeConfigurator isInMethodCast(
			boolean allowInMethodResultCast) {
		assertConfigurable(this.allowInMethodResultCast);
		this.allowInMethodResultCast = allowInMethodResultCast;

		return this;
	}

	@Override
	protected TypeToken<InputSequenceNode> getNodeClass() {
		return TypeToken.of(InputSequenceNode.class);
	}

	@Override
	public ChildrenConfigurator createChildrenConfigurator() {
		TypeToken<?> outputTarget = getContext().outputSourceType();

		return new SequentialChildrenConfigurator(
				new SchemaNodeConfigurationContext<ChildNode<?, ?>>() {
					@Override
					public BoundSet boundSet() {
						return getContext().boundSet();
					}

					@Override
					public DataLoader dataLoader() {
						return getDataLoader();
					}

					@Override
					public boolean isAbstract() {
						return isChildContextAbstract();
					}

					@Override
					public boolean isInputExpected() {
						return false;
					}

					@Override
					public boolean isInputDataOnly() {
						return getContext().isInputDataOnly();
					}

					@Override
					public boolean isConstructorExpected() {
						return false;
					}

					@Override
					public boolean isStaticMethodExpected() {
						return false;
					}

					@Override
					public Namespace namespace() {
						return getNamespace();
					}

					@Override
					public TypeToken<?> inputTargetType(QualifiedName node) {
						return null;
					}

					@Override
					public TypeToken<?> outputSourceType() {
						return outputTarget;
					}

					@Override
					public void addChild(ChildNode<?, ?> result) {}

					@Override
					public <U extends ChildNode<?, ?>> List<U> overrideChild(
							QualifiedName id, TypeToken<U> nodeClass) {
						return null;
					}

					@Override
					public List<? extends SchemaNode<?, ?>> overriddenNodes() {
						return getOverriddenNodes();
					}
				}) {
			@Override
			public ChildBuilder addChild() {
				ChildBuilder component = super.addChild();
				return new ChildBuilder() {
					@Override
					public ComplexNodeConfigurator<Object> complex() {
						return component.complex();
					}

					@Override
					public InputSequenceNodeConfigurator inputSequence() {
						return null;
					}

					@Override
					public SequenceNodeConfigurator sequence() {
						return null;
					}

					@Override
					public ChoiceNodeConfigurator choice() {
						return null;
					}

					@Override
					public DataNodeConfigurator<Object> data() {
						return component.data();
					}
				};
			}
		};
	}

	@Override
	protected boolean isChildContextAbstract() {
		return getContext().isAbstract() || super.isChildContextAbstract();
	}
}
