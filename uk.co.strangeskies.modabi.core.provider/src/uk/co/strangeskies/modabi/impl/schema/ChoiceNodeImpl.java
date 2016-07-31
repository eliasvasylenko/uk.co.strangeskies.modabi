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

import uk.co.strangeskies.modabi.schema.ChildNode;
import uk.co.strangeskies.modabi.schema.ChildNodeConfigurator;
import uk.co.strangeskies.modabi.schema.ChoiceNode;
import uk.co.strangeskies.modabi.schema.ChoiceNodeConfigurator;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.reflection.Types;

class ChoiceNodeImpl extends ChildNodeImpl<ChoiceNode> implements ChoiceNode {
	private final TypeToken<?> preInputClass;
	private final TypeToken<?> postInputClass;

	public ChoiceNodeImpl(ChoiceNodeConfiguratorImpl configurator) {
		super(configurator);

		TypeToken<?> preInputClass = null;
		TypeToken<?> postInputClass = configurator
				.getOverride(ChildNode::postInputType, ChildNodeConfigurator::getPostInputType)
				.validate(TypeToken::isAssignableTo).tryGet();

		if (concrete()) {
			preInputClass = TypeToken.over(Types.greatestLowerBound(
					children().stream().map(ChildNode::preInputType).map(TypeToken::getType).collect(toSet())));

			if (postInputClass == null) {
				postInputClass = TypeToken.over(Types.leastUpperBound(
						children().stream().map(ChildNode::postInputType).map(TypeToken::getType).collect(toSet())));
			}
		}

		this.preInputClass = preInputClass;
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

	@Override
	public ChoiceNodeConfigurator configurator() {
		return (ChoiceNodeConfigurator) super.configurator();
	}
}
