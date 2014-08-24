package uk.co.strangeskies.modabi.model.building.configurators.impl;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.model.building.ChildBuilder;
import uk.co.strangeskies.modabi.model.building.configurators.InputSequenceNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.impl.ChildNodeImpl;
import uk.co.strangeskies.modabi.model.building.impl.ChildrenConfigurator;
import uk.co.strangeskies.modabi.model.building.impl.OverrideMerge;
import uk.co.strangeskies.modabi.model.building.impl.SchemaNodeConfigurationContext;
import uk.co.strangeskies.modabi.model.building.impl.SequentialChildrenConfigurator;
import uk.co.strangeskies.modabi.model.nodes.BindingChildNode;
import uk.co.strangeskies.modabi.model.nodes.InputSequenceNode;
import uk.co.strangeskies.modabi.namespace.QualifiedName;

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

			protected Effective(
					OverrideMerge<InputSequenceNode, InputSequenceNodeConfiguratorImpl<?>> overrideMerge) {
				super(overrideMerge);

				inMethodName = overrideMerge.getValue(
						InputSequenceNode::getInMethodName, Objects::equals);

				inMethodChained = overrideMerge
						.getValue(InputSequenceNode::isInMethodChained);

				Method inMethod = null;
				try {
					Class<?> inputClass = overrideMerge.configurator().getContext()
							.getInputTargetClass();

					List<Class<?>> parameterClasses = overrideMerge.configurator()
							.getChildrenContainer().getChildren().stream()
							.map(o -> ((BindingChildNode<?, ?, ?>) o).getDataClass())
							.collect(Collectors.toList());

					inMethod = (inputClass == null || parameterClasses == null
							|| parameterClasses.stream().anyMatch(Objects::isNull) || inMethodName == null) ? null
							: inputClass.getMethod(inMethodName,
									parameterClasses.toArray(new Class[0]));
				} catch (NoSuchMethodException | SecurityException e) {
				}
				this.inMethod = inMethod;
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
		}

		private final Effective effective;

		private final String inMethodName;
		private final Boolean inMethodChained;

		public InputSequenceNodeImpl(
				InputSequenceNodeConfiguratorImpl<?> configurator) {
			super(configurator);

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
	}

	private String inMethodName;
	private Boolean inMethodChained;

	public InputSequenceNodeConfiguratorImpl(
			SchemaNodeConfigurationContext<? super InputSequenceNode> parent) {
		super(parent);
	}

	@Override
	public InputSequenceNodeConfigurator<C> name(String name) {
		return name(new QualifiedName(name, getContext().getNamespace()));
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
		Class<?> outputTarget = getContext().getOutputTargetClass();

		return new SequentialChildrenConfigurator<>(getNamespace(),
				getOverriddenNodes(), null, outputTarget, getDataLoader(), getContext()
						.isAbstract());
	}

	@Override
	public ChildBuilder<C, C> addChild() {
		return super.addChild();
	}
}
