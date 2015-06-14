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

import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.node.ChildNode;
import uk.co.strangeskies.modabi.schema.node.ChoiceNode;
import uk.co.strangeskies.modabi.schema.node.building.configuration.ChoiceNodeConfigurator;
import uk.co.strangeskies.modabi.schema.node.building.configuration.impl.utilities.ChildrenConfigurator;
import uk.co.strangeskies.modabi.schema.node.building.configuration.impl.utilities.OverrideMerge;
import uk.co.strangeskies.modabi.schema.node.building.configuration.impl.utilities.SchemaNodeConfigurationContext;
import uk.co.strangeskies.reflection.TypeToken;

public class ChoiceNodeConfiguratorImpl extends
		ChildNodeConfiguratorImpl<ChoiceNodeConfigurator, ChoiceNode> implements
		ChoiceNodeConfigurator {
	protected static class ChoiceNodeImpl extends
			SchemaNodeImpl<ChoiceNode, ChoiceNode.Effective> implements ChoiceNode {
		private static class Effective extends
				SchemaNodeImpl.Effective<ChoiceNode, ChoiceNode.Effective> implements
				ChoiceNode.Effective {
			private final boolean mandatory;

			private final TypeToken<?> preInputClass;
			private final TypeToken<?> postInputClass;

			public Effective(
					OverrideMerge<ChoiceNode, ChoiceNodeConfiguratorImpl> overrideMerge) {
				super(overrideMerge);

				TypeToken<?> preInputClass = null;
				if (!isAbstract())
					for (ChildNode.Effective<?, ?> child : children()) {
						TypeToken<?> nextInputClass = child.getPreInputType();
						if (preInputClass != null)
							if (preInputClass.isAssignableFrom(nextInputClass))
								preInputClass = nextInputClass;
							else if (!nextInputClass.isAssignableFrom(preInputClass))
								throw new IllegalArgumentException();
					}
				this.preInputClass = preInputClass;

				TypeToken<?> postInputClass = overrideMerge.tryGetValue(
						ChildNode::getPostInputType, TypeToken::isAssignableTo);
				if (!isAbstract())
					if (postInputClass == null)
						for (ChildNode.Effective<?, ?> child : children()) {
							TypeToken<?> nextOutputClass = child.getPostInputType();
							if (postInputClass != null)
								if (nextOutputClass.isAssignableFrom(postInputClass))
									postInputClass = nextOutputClass;
								else if (!postInputClass.isAssignableFrom(nextOutputClass))
									postInputClass = TypeToken.over(Object.class);
						}
					else
						for (ChildNode.Effective<?, ?> child : children())
							if (!postInputClass.isAssignableFrom(child.getPostInputType()))
								throw new SchemaException();
				this.postInputClass = postInputClass;

				mandatory = overrideMerge.getValue(ChoiceNode::isMandatory);
			}

			@Override
			public TypeToken<?> getPreInputType() {
				return preInputClass;
			}

			@Override
			public TypeToken<?> getPostInputType() {
				return postInputClass;
			}

			@Override
			public Boolean isMandatory() {
				return mandatory;
			}
		}

		private final Effective effective;

		private final TypeToken<?> postInputClass;
		private final boolean mandatory;

		public ChoiceNodeImpl(ChoiceNodeConfiguratorImpl configurator) {
			super(configurator);

			postInputClass = configurator.getPostInputClass();
			mandatory = configurator.mandatory;

			effective = new Effective(overrideMerge(this, configurator));
		}

		@Override
		public final Boolean isMandatory() {
			return mandatory;
		}

		@Override
		public Effective effective() {
			return effective;
		}

		@Override
		public TypeToken<?> getPostInputType() {
			return postInputClass;
		}
	}

	private boolean mandatory;

	public ChoiceNodeConfiguratorImpl(
			SchemaNodeConfigurationContext<? super ChildNode<?, ?>> parent) {
		super(parent);
	}

	@Override
	public ChoiceNode tryCreate() {
		return new ChoiceNodeImpl(this);
	}

	@Override
	public ChildrenConfigurator createChildrenConfigurator() {
		return null; // TODO create hiding children configurator! options can be
									// reduced, not increased, but overriding nodes.
	}

	@Override
	public ChoiceNodeConfigurator mandatory(boolean mandatory) {
		this.mandatory = mandatory;

		return this;
	}

	@Override
	protected TypeToken<ChoiceNode> getNodeClass() {
		return TypeToken.over(ChoiceNode.class);
	}
}
