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

import static java.util.stream.Collectors.toSet;

import uk.co.strangeskies.modabi.Abstractness;
import uk.co.strangeskies.modabi.impl.schema.utilities.OverrideMerge;
import uk.co.strangeskies.modabi.schema.ChildNode;
import uk.co.strangeskies.modabi.schema.ChoiceNode;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.reflection.Types;

class ChoiceNodeImpl extends ChildNodeImpl<ChoiceNode, ChoiceNode.Effective> implements ChoiceNode {
	private static class Effective extends ChildNodeImpl.Effective<ChoiceNode, ChoiceNode.Effective>
			implements ChoiceNode.Effective {
		private final TypeToken<?> preInputClass;
		private final TypeToken<?> postInputClass;

		public Effective(OverrideMerge<ChoiceNode, ChoiceNodeConfiguratorImpl> overrideMerge) {
			super(overrideMerge);

			TypeToken<?> preInputClass = null;
			if (abstractness().isLessThan(Abstractness.ABSTRACT))
				for (ChildNode.Effective<?, ?> child : children()) {
					TypeToken<?> nextInputClass = child.preInputType();
					if (preInputClass != null)
						if (preInputClass.isAssignableFrom(nextInputClass))
							preInputClass = nextInputClass;
						else if (!nextInputClass.isAssignableFrom(preInputClass))
							throw new IllegalArgumentException();
				}
			this.preInputClass = preInputClass;

			TypeToken<?> postInputClass = overrideMerge.getOverride(ChildNode::postInputType)
					.validate(TypeToken::isAssignableTo).tryGet();

			if (abstractness().isLessThan(Abstractness.ABSTRACT) && postInputClass == null) {
				postInputClass = TypeToken.over(Types.leastUpperBound(
						children().stream().map(ChildNode::postInputType).map(TypeToken::getType).collect(toSet())));
			}

			this.postInputClass = postInputClass;
		}

		@Override
		public TypeToken<?> preInputType() {
			return preInputClass;
		}

		@Override
		public TypeToken<?> postInputType() {
			return postInputClass;
		}
	}

	private final ChoiceNodeImpl.Effective effective;

	private final TypeToken<?> postInputClass;

	public ChoiceNodeImpl(ChoiceNodeConfiguratorImpl configurator) {
		super(configurator);

		postInputClass = configurator.getPostInputClass();

		effective = new Effective(ChoiceNodeConfiguratorImpl.overrideMerge(this, configurator));
	}

	@Override
	public ChoiceNodeImpl.Effective effective() {
		return effective;
	}

	@Override
	public TypeToken<?> postInputType() {
		return postInputClass;
	}
}
