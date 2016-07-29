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

import uk.co.strangeskies.modabi.ModabiException;
import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.impl.schema.utilities.ChildrenConfigurator;
import uk.co.strangeskies.modabi.impl.schema.utilities.SchemaNodeConfigurationContext;
import uk.co.strangeskies.modabi.impl.schema.utilities.SequentialChildrenConfigurator;
import uk.co.strangeskies.modabi.schema.ChoiceNodeConfigurator;
import uk.co.strangeskies.modabi.schema.ComplexNodeConfigurator;
import uk.co.strangeskies.modabi.schema.DataNodeConfigurator;
import uk.co.strangeskies.modabi.schema.InputNode.InputMemberType;
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
	private String inputMember;
	private InputMemberType inputMemberType;
	private Boolean chainedInput;
	private Boolean castIntput;
	private Boolean uncheckedInput;

	public InputSequenceNodeConfiguratorImpl(SchemaNodeConfigurationContext parent) {
		super(parent);
	}

	@Override
	public InputSequenceNode create() {
		return new InputSequenceNodeImpl(this);
	}

	@Override
	public final InputSequenceNodeConfigurator inputMethod(String methodName) {
		checkInputAllowed();
		this.inputMember = methodName;
		this.inputMemberType = InputMemberType.METHOD;
		return getThis();
	}

	@Override
	public final InputSequenceNodeConfigurator inputField(String fieldName) {
		checkInputAllowed();
		this.inputMember = fieldName;
		this.inputMemberType = InputMemberType.FIELD;
		return getThis();
	}

	@Override
	public InputSequenceNodeConfigurator inputNone() {
		this.inputMemberType = InputMemberType.NONE;
		return getThis();
	}

	private void checkInputAllowed() {
		if (!getContext().isInputExpected())
			throw new ModabiException(t -> t.cannotDefineInputInContext(getName()));
	}

	@Override
	public String getInputMember() {
		return inputMember;
	}

	@Override
	public InputMemberType getInputMemberType() {
		return inputMemberType;
	}

	@Override
	public final InputSequenceNodeConfigurator chainedInput(boolean chained) {
		this.chainedInput = chained;
		return getThis();
	}

	@Override
	public Boolean getChainedInput() {
		return chainedInput;
	}

	@Override
	public final InputSequenceNodeConfigurator castInput(boolean allowInMethodResultCast) {
		this.castIntput = allowInMethodResultCast;

		return getThis();
	}

	@Override
	public Boolean getCastInput() {
		return castIntput;
	}

	@Override
	public final InputSequenceNodeConfigurator uncheckedInput(boolean unchecked) {
		uncheckedInput = unchecked;

		return getThis();
	}

	@Override
	public Boolean getUncheckedInput() {
		return uncheckedInput;
	}

	@Override
	public ChildrenConfigurator createChildrenConfigurator() {
		TypeToken<?> outputTarget = getContext().outputSourceType();

		return new SequentialChildrenConfigurator(new SchemaNodeConfigurationContext() {
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
			public List<? extends SchemaNode<?>> overriddenNodes() {
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
