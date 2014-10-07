package uk.co.strangeskies.modabi.schema.node.building.configuration.impl;

import java.lang.reflect.Executable;
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
import uk.co.strangeskies.modabi.schema.node.building.configuration.DataNodeConfigurator;
import uk.co.strangeskies.modabi.schema.node.building.configuration.ElementNodeConfigurator;
import uk.co.strangeskies.modabi.schema.node.building.configuration.InputSequenceNodeConfigurator;
import uk.co.strangeskies.modabi.schema.node.building.configuration.SequenceNodeConfigurator;
import uk.co.strangeskies.modabi.schema.node.building.configuration.impl.utilities.ChildrenConfigurator;
import uk.co.strangeskies.modabi.schema.node.building.configuration.impl.utilities.OverrideMerge;
import uk.co.strangeskies.modabi.schema.node.building.configuration.impl.utilities.SchemaNodeConfigurationContext;
import uk.co.strangeskies.modabi.schema.node.building.configuration.impl.utilities.SequentialChildrenConfigurator;

public class InputSequenceNodeConfiguratorImpl<C extends BindingChildNode<?, ?, ?>>
		extends
		ChildNodeConfiguratorImpl<InputSequenceNodeConfigurator<C>, InputSequenceNode, C, C>
		implements InputSequenceNodeConfigurator<C> {
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

			private final Class<?> preInputClass;
			private final Class<?> postInputClass;

			protected Effective(
					OverrideMerge<InputSequenceNode, InputSequenceNodeConfiguratorImpl<?>> overrideMerge) {
				super(overrideMerge);

				if (!overrideMerge.configurator().getContext().isInputExpected())
					throw new SchemaException("InputSequenceNode '" + getName()
							+ "' cannot occur in a context without input.");

				InputNodeConfigurationHelper<InputSequenceNode, InputSequenceNode.Effective> inputNodeHelper = new InputNodeConfigurationHelper<>(
						this, overrideMerge, overrideMerge.configurator().getContext());
				inMethodChained = inputNodeHelper.isInMethodChained();
				allowInMethodResultCast = inputNodeHelper.isInMethodCast();
				List<Class<?>> parameterClasses = overrideMerge
						.configurator()
						.getChildrenContainer()
						.getChildren()
						.stream()
						.map(
								o -> ((BindingChildNode<?, ?, ?>) o).effective().getDataClass())
						.collect(Collectors.toList());
				inMethod = inputNodeHelper.inMethod(parameterClasses);
				inMethodName = inputNodeHelper.inMethodName();
				preInputClass = inputNodeHelper.preInputClass();
				postInputClass = inputNodeHelper.postInputClass();
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
			public Class<?> getPostInputClass() {
				return postInputClass;
			}

			@Override
			public Class<?> getPreInputClass() {
				return preInputClass;
			}
		}

		private final Effective effective;

		private final Class<?> postInputClass;
		private final String inMethodName;
		private final Boolean inMethodChained;
		private final Boolean allowInMethodResultCast;

		public InputSequenceNodeImpl(
				InputSequenceNodeConfiguratorImpl<?> configurator) {
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
		public Class<?> getPostInputClass() {
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
	public InputSequenceNodeConfigurator<C> inMethod(String methodName) {
		if (!getContext().isInputExpected() && !inMethodName.equals("null"))
			throw new SchemaException(
					"No input method should be specified on this node.");

		requireConfigurable(inMethodName);
		inMethodName = methodName;

		return this;
	}

	@Override
	public InputSequenceNodeConfigurator<C> inMethodChained(boolean chained) {
		requireConfigurable(inMethodChained);
		inMethodChained = chained;

		return this;
	}

	@Override
	public InputSequenceNodeConfigurator<C> isInMethodCast(
			boolean allowInMethodResultCast) {
		requireConfigurable(this.allowInMethodResultCast);
		this.allowInMethodResultCast = allowInMethodResultCast;

		return this;
	}

	@Override
	protected Class<InputSequenceNode> getNodeClass() {
		return InputSequenceNode.class;
	}

	@Override
	public ChildrenConfigurator<C, C> createChildrenConfigurator() {
		Class<?> outputTarget = getContext().outputSourceClass();

		return new SequentialChildrenConfigurator<C, C>(
				new SchemaNodeConfigurationContext<ChildNode<?, ?>>() {
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
					public Namespace namespace() {
						return getNamespace();
					}

					@Override
					public Class<?> inputTargetClass(QualifiedName node) {
						return null;
					}

					@Override
					public Class<?> outputSourceClass() {
						return outputTarget;
					}

					@Override
					public void addChild(ChildNode<?, ?> result) {
					}

					@Override
					public <U extends ChildNode<?, ?>> List<U> overrideChild(
							QualifiedName id, Class<U> nodeClass) {
						return null;
					}

					@Override
					public List<? extends SchemaNode<?, ?>> overriddenNodes() {
						return getOverriddenNodes();
					}
				}) {
			@Override
			public ChildBuilder<C, C> addChild() {
				ChildBuilder<C, C> component = super.addChild();
				return new ChildBuilder<C, C>() {
					@Override
					public ElementNodeConfigurator<Object> element() {
						return component.element();
					}

					@Override
					public InputSequenceNodeConfigurator<C> inputSequence() {
						return null;
					}

					@Override
					public SequenceNodeConfigurator<C, C> sequence() {
						return null;
					}

					@Override
					public ChoiceNodeConfigurator<C, C> choice() {
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
