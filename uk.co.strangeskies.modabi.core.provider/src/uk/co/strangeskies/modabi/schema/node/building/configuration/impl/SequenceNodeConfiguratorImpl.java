package uk.co.strangeskies.modabi.schema.node.building.configuration.impl;

import java.lang.reflect.Type;
import java.util.List;

import uk.co.strangeskies.modabi.namespace.Namespace;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.node.ChildNode;
import uk.co.strangeskies.modabi.schema.node.SchemaNode;
import uk.co.strangeskies.modabi.schema.node.SequenceNode;
import uk.co.strangeskies.modabi.schema.node.building.DataLoader;
import uk.co.strangeskies.modabi.schema.node.building.configuration.SequenceNodeConfigurator;
import uk.co.strangeskies.modabi.schema.node.building.configuration.impl.utilities.ChildrenConfigurator;
import uk.co.strangeskies.modabi.schema.node.building.configuration.impl.utilities.OverrideMerge;
import uk.co.strangeskies.modabi.schema.node.building.configuration.impl.utilities.SchemaNodeConfigurationContext;
import uk.co.strangeskies.modabi.schema.node.building.configuration.impl.utilities.SequentialChildrenConfigurator;
import uk.co.strangeskies.reflection.TypeLiteral;

public class SequenceNodeConfiguratorImpl extends
		ChildNodeConfiguratorImpl<SequenceNodeConfigurator, SequenceNode> implements
		SequenceNodeConfigurator {
	protected static class SequenceNodeImpl extends
			SchemaNodeImpl<SequenceNode, SequenceNode.Effective> implements
			SequenceNode {
		private class Effective extends
				SchemaNodeImpl.Effective<SequenceNode, SequenceNode.Effective>
				implements SequenceNode.Effective {
			private final Type preInputClass;
			private final Type postInputClass;

			public Effective(
					OverrideMerge<SequenceNode, SequenceNodeConfiguratorImpl> overrideMerge) {
				super(overrideMerge);

				preInputClass = isAbstract() ? null : children().get(0)
						.getPreInputType();

				Type postInputClass = overrideMerge.tryGetValue(
						ChildNode::getPostInputType, (n, o) -> TypeLiteral.from(o)
								.isAssignableFrom(n));
				if (postInputClass == null && !isAbstract()) {
					for (ChildNode.Effective<?, ?> child : children()) {
						if (postInputClass != null
								&& !TypeLiteral.from(child.getPreInputType()).isAssignableFrom(
										postInputClass)) {
							throw new IllegalArgumentException();
						}
						postInputClass = child.getPostInputType();
					}
				}
				this.postInputClass = postInputClass;
			}

			@Override
			public Type getPreInputType() {
				return preInputClass;
			}

			@Override
			public Type getPostInputType() {
				return postInputClass;
			}
		}

		private final Effective effective;

		private final Type postInputClass;

		public SequenceNodeImpl(SequenceNodeConfiguratorImpl configurator) {
			super(configurator);

			postInputClass = configurator.getPostInputClass();

			effective = new Effective(overrideMerge(this, configurator));
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

	public SequenceNodeConfiguratorImpl(
			SchemaNodeConfigurationContext<? super SequenceNode> parent) {
		super(parent);
	}

	@Override
	public SequenceNode tryCreate() {
		return new SequenceNodeImpl(this);
	}

	@Override
	protected TypeLiteral<SequenceNode> getNodeClass() {
		return TypeLiteral.from(SequenceNode.class);
	}

	@Override
	public ChildrenConfigurator createChildrenConfigurator() {
		TypeLiteral<?> inputTarget = getContext().inputTargetType(getName());

		return new SequentialChildrenConfigurator(
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
						return true;
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
					public TypeLiteral<?> inputTargetType(QualifiedName node) {
						return inputTarget;
					}

					@Override
					public TypeLiteral<?> outputSourceType() {
						return null;
					}

					@Override
					public void addChild(ChildNode<?, ?> result) {}

					@Override
					public <U extends ChildNode<?, ?>> List<U> overrideChild(
							QualifiedName id, TypeLiteral<U> nodeClass) {
						return null;
					}

					@Override
					public List<? extends SchemaNode<?, ?>> overriddenNodes() {
						return getOverriddenNodes();
					}
				});
	}

	@Override
	protected boolean isChildContextAbstract() {
		return getContext().isAbstract() || super.isChildContextAbstract();
	}
}
