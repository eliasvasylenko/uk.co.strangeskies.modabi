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

import java.util.Objects;

import uk.co.strangeskies.mathematics.Range;
import uk.co.strangeskies.modabi.impl.schema.utilities.OverrideMerge;
import uk.co.strangeskies.modabi.schema.ChildNode;
import uk.co.strangeskies.modabi.schema.SchemaNode;

public abstract class ChildNodeImpl<S extends ChildNode<S, E>, E extends ChildNode.Effective<S, E>>
		extends SchemaNodeImpl<S, E> implements ChildNode<S, E> {
	protected static abstract class Effective<S extends ChildNode<S, E>, E extends ChildNode.Effective<S, E>>
			extends SchemaNodeImpl.Effective<S, E>
			implements ChildNode.Effective<S, E> {
		private final SchemaNode.Effective<?, ?> parent;

		private final Range<Integer> occurrences;
		private final Boolean ordered;

		public Effective(
				OverrideMerge<S, ? extends ChildNodeConfiguratorImpl<?, S>> overrideMerge) {
			super(overrideMerge);

			parent = overrideMerge.configurator().getContext().parentNodeProxy()
					.effective();

			ordered = overrideMerge.getOverride(ChildNode::isOrdered).orDefault(true)
					.get();

			occurrences = overrideMerge.getOverride(ChildNode::occurrences)
					.validate((v, o) -> o.contains(v)).orDefault(Range.between(1, 1))
					.get();

		}

		@Override
		public SchemaNode.Effective<?, ?> parent() {
			return parent;
		}

		@Override
		public Range<Integer> occurrences() {
			return occurrences;
		}

		@Override
		public Boolean isOrdered() {
			return ordered;
		}

		@Override
		public boolean equals(Object that) {
			return super.equals(that) && that instanceof ChildNode.Effective
					&& Objects.equals(parent(),
							((ChildNode.Effective<?, ?>) that).parent());
		}

		@Override
		public final int hashCode() {
			return super.hashCode() ^ Objects.hashCode(parent());
		}
	}

	private final SchemaNode<?, ?> parent;

	private final Range<Integer> occurrences;
	private final Boolean ordered;

	public ChildNodeImpl(ChildNodeConfiguratorImpl<?, ?> configurator) {
		super(configurator);

		parent = configurator.getContext().parentNodeProxy();

		ordered = configurator.getOrdered();
		occurrences = configurator.getOccurrences();
	}

	@Override
	public SchemaNode<?, ?> parent() {
		return parent;
	}

	@Override
	public Boolean isOrdered() {
		return ordered;
	}

	@Override
	public final Range<Integer> occurrences() {
		return occurrences;
	}

	@Override
	public boolean equals(Object that) {
		return super.equals(that) && that instanceof ChildNode
				&& Objects.equals(parent(), ((ChildNode<?, ?>) that).parent());
	}

	@Override
	public final int hashCode() {
		return super.hashCode() ^ Objects.hashCode(parent());
	}
}
