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
import uk.co.strangeskies.reflection.TypeToken;

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
						ChildNode::getPostInputType, (n, o) -> TypeToken.of(o)
								.isAssignableFrom(n));
				if (postInputClass == null && !isAbstract()) {
					for (ChildNode.Effective<?, ?> child : children()) {
						if (postInputClass != null
								&& !TypeToken.of(child.getPreInputType()).isAssignableFrom(
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
	protected TypeToken<SequenceNode> getNodeClass() {
		return TypeToken.of(SequenceNode.class);
	}

	@Override
	public ChildrenConfigurator createChildrenConfigurator() {
		TypeToken<?> inputTarget = getContext().inputTargetType(getName());

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
					public TypeToken<?> inputTargetType(QualifiedName node) {
						return inputTarget;
					}

					@Override
					public TypeToken<?> outputSourceType() {
						return null;
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
				});
	}

	@Override
	protected boolean isChildContextAbstract() {
		return getContext().isAbstract() || super.isChildContextAbstract();
	}
}
