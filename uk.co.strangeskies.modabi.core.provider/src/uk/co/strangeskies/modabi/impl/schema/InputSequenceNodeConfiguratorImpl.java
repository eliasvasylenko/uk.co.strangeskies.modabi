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

import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.impl.schema.utilities.ChildrenConfigurator;
import uk.co.strangeskies.modabi.impl.schema.utilities.SchemaNodeConfigurationContext;
import uk.co.strangeskies.modabi.impl.schema.utilities.SequentialChildrenConfigurator;
import uk.co.strangeskies.modabi.schema.ChoiceNodeConfigurator;
import uk.co.strangeskies.modabi.schema.ComplexNodeConfigurator;
import uk.co.strangeskies.modabi.schema.DataNodeConfigurator;
import uk.co.strangeskies.modabi.schema.InputSequenceNode;
import uk.co.strangeskies.modabi.schema.InputSequenceNodeConfigurator;
import uk.co.strangeskies.modabi.schema.SchemaNode;
import uk.co.strangeskies.modabi.schema.SequenceNodeConfigurator;
import uk.co.strangeskies.modabi.schema.building.ChildBuilder;
import uk.co.strangeskies.modabi.schema.building.DataLoader;
import uk.co.strangeskies.reflection.BoundSet;
import uk.co.strangeskies.reflection.Imports;
import uk.co.strangeskies.reflection.TypeToken;

public class InputSequenceNodeConfiguratorImpl
		extends ChildNodeConfiguratorImpl<InputSequenceNodeConfigurator, InputSequenceNode>
		implements InputSequenceNodeConfigurator {
	private String inMethodName;
	private Boolean inMethodChained;
	private Boolean inMethodCast;
	private Boolean inMethodUnchecked;

	public InputSequenceNodeConfiguratorImpl(SchemaNodeConfigurationContext parent) {
		super(parent);
	}

	@Override
	public InputSequenceNode tryCreateImpl() {
		return new InputSequenceNodeImpl(this);
	}

	@Override
	public InputSequenceNodeConfigurator inMethod(String methodName) {
		if (!getContext().isInputExpected() && !inMethodName.equals("null"))
			throw new SchemaException("No input method should be specified on this node.");

		assertConfigurable(inMethodName);
		inMethodName = methodName;

		return this;
	}

	public String getInMethodName() {
		return inMethodName;
	}

	@Override
	public InputSequenceNodeConfigurator inMethodChained(boolean chained) {
		assertConfigurable(inMethodChained);
		inMethodChained = chained;

		return this;
	}

	public Boolean getInMethodChained() {
		return inMethodChained;
	}

	@Override
	public InputSequenceNodeConfigurator inMethodCast(boolean allowInMethodResultCast) {
		assertConfigurable(this.inMethodCast);
		this.inMethodCast = allowInMethodResultCast;

		return this;
	}

	public Boolean getInMethodCast() {
		return inMethodCast;
	}

	@Override
	public final InputSequenceNodeConfigurator inMethodUnchecked(boolean unchecked) {
		assertConfigurable(inMethodUnchecked);
		inMethodUnchecked = unchecked;

		return getThis();
	}

	public Boolean getInMethodUnchecked() {
		return inMethodUnchecked;
	}

	@Override
	protected TypeToken<InputSequenceNode> getNodeClass() {
		return TypeToken.over(InputSequenceNode.class);
	}

	@Override
	public ChildrenConfigurator createChildrenConfigurator() {
		TypeToken<?> outputTarget = getContext().outputSourceType();

		return new SequentialChildrenConfigurator(new SchemaNodeConfigurationContext() {
			@Override
			public SchemaNode<?, ?> parentNodeProxy() {
				return getSchemaNodeProxy();
			}

			@Override
			public BoundSet boundSet() {
				return getContext().boundSet();
			}

			@Override
			public DataLoader dataLoader() {
				return getDataLoader();
			}

			@Override
			public Imports imports() {
				return getImports();
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
			public TypeToken<?> inputTargetType() {
				return null;
			}

			@Override
			public TypeToken<?> outputSourceType() {
				return outputTarget;
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
						throw new UnsupportedOperationException();
					}

					@Override
					public SequenceNodeConfigurator sequence() {
						throw new UnsupportedOperationException();
					}

					@Override
					public ChoiceNodeConfigurator choice() {
						throw new UnsupportedOperationException();
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
