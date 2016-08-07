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
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.schema.ChildNode;
import uk.co.strangeskies.modabi.schema.ChildNodeConfigurator;
import uk.co.strangeskies.modabi.schema.RootNode;
import uk.co.strangeskies.modabi.schema.SchemaNode;

public abstract class ChildNodeImpl<S extends ChildNode<S>> extends SchemaNodeImpl<S> implements ChildNode<S> {
	private final SchemaNode<?> parent;

	private final Range<Integer> occurrences;
	private final Boolean ordered;

	public <C extends ChildNodeConfigurator<C, S>> ChildNodeImpl(ChildNodeConfiguratorImpl<C, S> configurator) {
		super(configurator);

		parent = configurator.getContext().parent();

		ordered = configurator.getOverride(ChildNode::ordered, ChildNodeConfigurator::getOrdered).orDefault(true).get();

		occurrences = configurator.getOverride(ChildNode::occurrences, ChildNodeConfigurator::getOccurrences)
				.validate((v, o) -> o.contains(v)).orDefault(Range.between(1, 1)).get();
	}

	@Override
	public ChildNodeConfigurator<?, S> configurator() {
		return (ChildNodeConfigurator<?, S>) super.configurator();
	}

	@Override
	public SchemaNode<?> parent() {
		return parent;
	}

	@Override
	public Range<Integer> occurrences() {
		return occurrences;
	}

	@Override
	public Boolean ordered() {
		return ordered;
	}

	@Override
	public boolean equals(Object that) {
		return super.equals(that) && that instanceof ChildNode<?>
				&& Objects.equals(parent(), ((ChildNode<?>) that).parent());
	}

	@Override
	public final int hashCode() {
		return super.hashCode() ^ Objects.hashCode(parent());
	}

	@Override
	public Schema schema() {
		return root().schema();
	}

	@Override
	public RootNode<?, ?> root() {
		return parent().root();
	}
}
