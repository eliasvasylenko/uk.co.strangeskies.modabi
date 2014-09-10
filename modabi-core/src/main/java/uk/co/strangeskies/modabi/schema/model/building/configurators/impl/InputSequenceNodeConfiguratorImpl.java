package uk.co.strangeskies.modabi.schema.model.building.configurators.impl;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.schema.model.building.ChildBuilder;
import uk.co.strangeskies.modabi.schema.model.building.configurators.InputSequenceNodeConfigurator;
import uk.co.strangeskies.modabi.schema.model.building.impl.ChildNodeImpl;
import uk.co.strangeskies.modabi.schema.model.building.impl.ChildrenConfigurator;
import uk.co.strangeskies.modabi.schema.model.building.impl.Methods;
import uk.co.strangeskies.modabi.schema.model.building.impl.OverrideMerge;
import uk.co.strangeskies.modabi.schema.model.building.impl.SchemaNodeConfigurationContext;
import uk.co.strangeskies.modabi.schema.model.building.impl.SequentialChildrenConfigurator;
import uk.co.strangeskies.modabi.schema.model.nodes.BindingChildNode;
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
			private final String inMethodName;
			private final Method inMethod;
			private final Boolean inMethodChained;

			private final Class<?> preInputClass;
			private final Class<?> postInputClass;

			protected Effective(
					OverrideMerge<InputSequenceNode, InputSequenceNodeConfiguratorImpl<?>> overrideMerge) {
				super(overrideMerge);

				String inMethodName = overrideMerge
						.getValue(InputSequenceNode::getInMethodName);
				this.inMethodName = inMethodName != null ? inMethodName
						: (isAbstract() ? null : getName()).getName();

				inMethodChained = overrideMerge
						.getValue(InputSequenceNode::isInMethodChained);

				List<Class<?>> parameterClasses = overrideMerge.configurator()
						.getChildrenContainer().getChildren().stream()
						.map(o -> ((BindingChildNode<?, ?, ?>) o).getDataClass())
						.collect(Collectors.toList());

				Method overriddenMethod = overrideMerge
						.getValue(n -> n.effective() == null ? (Method) null : n
								.effective().getInMethod());
				inMethod = (isAbstract() || inMethodName == "null") ? null : Methods
						.getInMethod(this, overriddenMethod, overrideMerge.configurator()
								.getContext().getInputTargetClass(getName()), parameterClasses);

				preInputClass = isAbstract() ? null : inMethod.getDeclaringClass();

				postInputClass = !isInMethodChained() ? getPreInputClass()
						: isAbstract() ? null : inMethod.getReturnType();
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

		public InputSequenceNodeImpl(
				InputSequenceNodeConfiguratorImpl<?> configurator) {
			super(configurator);

			postInputClass = configurator.getPostInputClass();
			inMethodName = configurator.inMethodName;
			inMethodChained = configurator.inMethodChained;

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
		inMethodName = methodName;

		return this;
	}

	@Override
	public InputSequenceNodeConfigurator<C> inMethodChained(boolean chained) {
		inMethodChained = chained;

		return this;
	}

	@Override
	protected Class<InputSequenceNode> getNodeClass() {
		return InputSequenceNode.class;
	}

	@Override
	public ChildrenConfigurator<C, C> createChildrenConfigurator() {
		Class<?> outputTarget = getContext().getOutputSourceClass();

		return new SequentialChildrenConfigurator<>(getNamespace(),
				getOverriddenNodes(), null, outputTarget, getDataLoader(), getContext()
						.isAbstract());
	}

	@Override
	public ChildBuilder<C, C> addChild() {
		return super.addChild();
	}
}
