package uk.co.strangeskies.modabi.schema.model.building.configurators.impl;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.model.building.ChildBuilder;
import uk.co.strangeskies.modabi.schema.model.building.configurators.ChoiceNodeConfigurator;
import uk.co.strangeskies.modabi.schema.model.building.configurators.DataNodeConfigurator;
import uk.co.strangeskies.modabi.schema.model.building.configurators.ElementNodeConfigurator;
import uk.co.strangeskies.modabi.schema.model.building.configurators.InputNodeConfigurator;
import uk.co.strangeskies.modabi.schema.model.building.configurators.InputSequenceNodeConfigurator;
import uk.co.strangeskies.modabi.schema.model.building.configurators.SequenceNodeConfigurator;
import uk.co.strangeskies.modabi.schema.model.building.impl.ChildNodeImpl;
import uk.co.strangeskies.modabi.schema.model.building.impl.ChildrenConfigurator;
import uk.co.strangeskies.modabi.schema.model.building.impl.Methods;
import uk.co.strangeskies.modabi.schema.model.building.impl.OverrideMerge;
import uk.co.strangeskies.modabi.schema.model.building.impl.SchemaNodeConfigurationContext;
import uk.co.strangeskies.modabi.schema.model.building.impl.SequentialChildrenConfigurator;
import uk.co.strangeskies.modabi.schema.model.nodes.BindingChildNode;
import uk.co.strangeskies.modabi.schema.model.nodes.InputNode;
import uk.co.strangeskies.modabi.schema.model.nodes.InputSequenceNode;

public class InputSequenceNodeConfiguratorImpl<C extends BindingChildNode<?, ?, ?>>
		extends
		ChildNodeConfiguratorImpl<InputSequenceNodeConfigurator<C>, InputSequenceNode, C, C>
		implements InputSequenceNodeConfigurator<C> {
	protected static class InputSequenceNodeImpl extends
			SchemaNodeImpl<InputSequenceNode, InputSequenceNode.Effective> implements
			ChildNodeImpl<InputSequenceNode, InputSequenceNode.Effective>,
			InputSequenceNode {
		private static class Effective
				extends
				SchemaNodeImpl.Effective<InputSequenceNode, InputSequenceNode.Effective>
				implements InputSequenceNode.Effective {
			private String inMethodName;
			private final Method inMethod;
			private final Boolean inMethodChained;
			private final Boolean allowInMethodResultCast;
			private final Boolean isInMethodIterable;

			private final Class<?> preInputClass;
			private final Class<?> postInputClass;

			protected Effective(
					OverrideMerge<InputSequenceNode, InputSequenceNodeConfiguratorImpl<?>> overrideMerge) {
				super(overrideMerge);

				inMethodChained = overrideMerge.getValue(
						InputSequenceNode::isInMethodChained, false);

				allowInMethodResultCast = inMethodChained != null && !inMethodChained ? null
						: overrideMerge.getValue(InputSequenceNode::isInMethodCast, false);

				isInMethodIterable = inMethodChained != null && !inMethodChained ? null
						: overrideMerge.getValue(InputSequenceNode::isInMethodIterable,
								false);

				List<Class<?>> parameterClasses = overrideMerge
						.configurator()
						.getChildrenContainer()
						.getChildren()
						.stream()
						.map(
								o -> ((BindingChildNode<?, ?, ?>) o).effective().getDataClass())
						.collect(Collectors.toList());

				Class<?> inputTargetClass = overrideMerge.configurator().getContext()
						.getInputTargetClass(getName());

				inMethodName = overrideMerge
						.tryGetValue(InputSequenceNode::getInMethodName);

				if (!overrideMerge.configurator().getContext().hasInput())
					throw new SchemaException(
							"It doesn't make sense to have inputSequence node '" + getName()
									+ "' occur in a context without input.");

				Method overriddenMethod = overrideMerge
						.tryGetValue(n -> n.effective() == null ? (Method) null : n
								.effective().getInMethod());
				inMethod = isAbstract() ? null : Methods.getInMethod(this,
						overriddenMethod, inputTargetClass, parameterClasses);

				if (inMethodName == null && !isAbstract())
					inMethodName = inMethod.getName();

				preInputClass = isAbstract() ? null : inMethod.getDeclaringClass();

				postInputClass = effectivePostInputClass(isAbstract(),
						inputTargetClass, inMethodName, inMethod, inMethodChained,
						isInMethodIterable, overrideMerge);
			}

			@Override
			public final String getInMethodName() {
				return inMethodName;
			}

			@Override
			public Method getInMethod() {
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
			public Boolean isInMethodIterable() {
				return isInMethodIterable;
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
		private final Boolean isInMethodIterable;

		public InputSequenceNodeImpl(
				InputSequenceNodeConfiguratorImpl<?> configurator) {
			super(configurator);

			postInputClass = configurator.getPostInputClass();
			inMethodName = configurator.inMethodName;
			inMethodChained = configurator.inMethodChained;
			allowInMethodResultCast = configurator.allowInMethodResultCast;
			isInMethodIterable = configurator.isInMethodIterable;

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
		public Boolean isInMethodIterable() {
			return isInMethodIterable;
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
	private Boolean isInMethodIterable;

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
		if (!getContext().hasInput() && !inMethodName.equals("null"))
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
	public InputSequenceNodeConfigurator<C> isInMethodIterable(
			boolean isInMethodIterable) {
		requireConfigurable(this.isInMethodIterable);
		this.isInMethodIterable = isInMethodIterable;

		return this;
	}

	@Override
	protected Class<InputSequenceNode> getNodeClass() {
		return InputSequenceNode.class;
	}

	@Override
	public ChildrenConfigurator<C, C> createChildrenConfigurator() {
		Class<?> outputTarget = getContext().getOutputSourceClass();

		return new SequentialChildrenConfigurator<C, C>(getNamespace(),
				getOverriddenNodes(), false, null, outputTarget, getDataLoader(),
				isChildContextAbstract(), getContext().isDataContext()) {
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

	static final Class<?> effectivePostInputClass(
			boolean isAbstract,
			Class<?> inputTargetClass,
			String inMethodName,
			Method inMethod,
			Boolean inMethodChained,
			Boolean inMethodIterable,
			OverrideMerge<? extends InputNode<?, ?>, ? extends InputNodeConfigurator<?, ?, ?, ?>> overrideMerge) {
		Class<?> postInputClass;

		if ("null".equals(inMethodName)
				|| (inMethodChained != null && !inMethodChained))
			postInputClass = inputTargetClass;
		else if (isAbstract)
			postInputClass = overrideMerge.tryGetValue(InputNode::getPostInputClass,
					(n, o) -> o.isAssignableFrom(n));
		else {
			Class<?> methodReturn = inMethodIterable ? Object.class : inMethod
					.getReturnType();

			Class<?> localPostInputClass = overrideMerge.node().getPostInputClass();
			if (localPostInputClass == null
					|| localPostInputClass.isAssignableFrom(methodReturn))
				localPostInputClass = methodReturn;
			postInputClass = overrideMerge.getValueWithOverride(localPostInputClass,
					InputNode::getPostInputClass, (n, o) -> o.isAssignableFrom(n));
		}

		return postInputClass;
	}
}
