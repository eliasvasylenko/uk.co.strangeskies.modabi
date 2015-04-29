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

			private final Type preInputClass;
			private final Type postInputClass;

			public Effective(
					OverrideMerge<ChoiceNode, ChoiceNodeConfiguratorImpl> overrideMerge) {
				super(overrideMerge);

				Type preInputClass = null;
				if (!isAbstract())
					for (ChildNode.Effective<?, ?> child : children()) {
						Type nextInputClass = child.getPreInputType();
						if (preInputClass != null)
							if (TypeToken.over(preInputClass).isAssignableFrom(nextInputClass))
								preInputClass = nextInputClass;
							else if (!TypeToken.over(nextInputClass).isAssignableFrom(
									preInputClass))
								throw new IllegalArgumentException();
					}
				this.preInputClass = preInputClass;

				Type postInputClass = overrideMerge.tryGetValue(
						ChildNode::getPostInputType, (n, o) -> TypeToken.over(o)
								.isAssignableFrom(n));
				if (!isAbstract())
					if (postInputClass == null)
						for (ChildNode.Effective<?, ?> child : children()) {
							Type nextOutputClass = child.getPostInputType();
							if (postInputClass != null)
								if (TypeToken.over(nextOutputClass).isAssignableFrom(
										postInputClass))
									postInputClass = nextOutputClass;
								else if (!TypeToken.over(postInputClass).isAssignableFrom(
										nextOutputClass))
									postInputClass = Object.class;
						}
					else
						for (ChildNode.Effective<?, ?> child : children())
							if (!TypeToken.over(postInputClass).isAssignableFrom(
									child.getPostInputType()))
								throw new SchemaException();
				this.postInputClass = postInputClass;

				mandatory = overrideMerge.getValue(ChoiceNode::isMandatory);
			}

			@Override
			public Type getPreInputType() {
				return preInputClass;
			}

			@Override
			public Type getPostInputType() {
				return postInputClass;
			}

			@Override
			public Boolean isMandatory() {
				return mandatory;
			}
		}

		private final Effective effective;

		private final Type postInputClass;
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
		public Type getPostInputType() {
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
