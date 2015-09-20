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
package uk.co.strangeskies.modabi.impl.schema;

import uk.co.strangeskies.modabi.impl.schema.utilities.OverrideMerge;
import uk.co.strangeskies.modabi.schema.ChildNode;
import uk.co.strangeskies.modabi.schema.SequenceNode;
import uk.co.strangeskies.reflection.TypeToken;

class SequenceNodeImpl extends
		SchemaNodeImpl<SequenceNode, SequenceNode.Effective> implements
		SequenceNode {
	private class Effective extends
			SchemaNodeImpl.Effective<SequenceNode, SequenceNode.Effective> implements
			SequenceNode.Effective {
		private final TypeToken<?> preInputClass;
		private final TypeToken<?> postInputClass;

		public Effective(
				OverrideMerge<SequenceNode, SequenceNodeConfiguratorImpl> overrideMerge) {
			super(overrideMerge);

			preInputClass = isAbstract() ? null : children().get(0).getPreInputType();

			TypeToken<?> postInputClass = overrideMerge.tryGetValue(
					ChildNode::getPostInputType, TypeToken::isAssignableTo);
			if (postInputClass == null && !isAbstract()) {
				for (ChildNode.Effective<?, ?> child : children()) {
					if (postInputClass != null
							&& !child.getPreInputType().isAssignableFrom(postInputClass)) {
						throw new IllegalArgumentException();
					}
					postInputClass = child.getPostInputType();
				}
			}
			this.postInputClass = postInputClass;
		}

		@Override
		public TypeToken<?> getPreInputType() {
			return preInputClass;
		}

		@Override
		public TypeToken<?> getPostInputType() {
			return postInputClass;
		}
	}

	private final Effective effective;
	private final TypeToken<?> postInputClass;

	public SequenceNodeImpl(SequenceNodeConfiguratorImpl configurator) {
		super(configurator);

		postInputClass = configurator.getPostInputClass();

		effective = new Effective(SequenceNodeConfiguratorImpl.overrideMerge(this,
				configurator));
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
