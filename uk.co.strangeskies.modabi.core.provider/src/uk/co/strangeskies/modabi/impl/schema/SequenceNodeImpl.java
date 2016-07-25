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

import uk.co.strangeskies.modabi.Abstractness;
import uk.co.strangeskies.modabi.schema.ChildNode;
import uk.co.strangeskies.modabi.schema.SequenceNode;
import uk.co.strangeskies.modabi.schema.SequenceNodeConfigurator;
import uk.co.strangeskies.reflection.TypeToken;

public class SequenceNodeImpl extends ChildNodeImpl<SequenceNode> implements SequenceNode {
	private final TypeToken<?> preInputClass;
	private final TypeToken<?> postInputClass;

	public SequenceNodeImpl(SequenceNodeConfiguratorImpl configurator) {
		super(configurator);

		preInputClass = abstractness().isLessThan(Abstractness.ABSTRACT) ? null : children().get(0).preInputType();

		TypeToken<?> postInputClass = configurator.getOverride(ChildNode::postInputType).validate(TypeToken::isAssignableTo)
				.tryGet();
		if (postInputClass == null && abstractness().isLessThan(Abstractness.ABSTRACT)) {
			postInputClass = children().get(children().size() - 1).postInputType();
		}
		this.postInputClass = postInputClass;
	}

	@Override
	public SequenceNodeConfigurator configurator() {
		return (SequenceNodeConfigurator) super.configurator();
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
