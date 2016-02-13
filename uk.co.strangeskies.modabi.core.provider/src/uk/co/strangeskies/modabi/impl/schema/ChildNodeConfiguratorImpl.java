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

import java.util.Collections;
import java.util.List;

import uk.co.strangeskies.mathematics.Range;
import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.impl.schema.utilities.SchemaNodeConfigurationContext;
import uk.co.strangeskies.modabi.schema.ChildNode;
import uk.co.strangeskies.modabi.schema.ChildNodeConfigurator;
import uk.co.strangeskies.modabi.schema.building.DataLoader;
import uk.co.strangeskies.reflection.TypeToken;

public abstract class ChildNodeConfiguratorImpl<S extends ChildNodeConfigurator<S, N>, N extends ChildNode<?, ?>>
		extends SchemaNodeConfiguratorImpl<S, N>
		implements ChildNodeConfigurator<S, N> {
	private final SchemaNodeConfigurationContext<? super N> context;

	private Range<Integer> occurrences;
	private Boolean ordered;
	private TypeToken<?> postInputClass;

	public ChildNodeConfiguratorImpl(
			SchemaNodeConfigurationContext<? super N> parent) {
		this.context = parent;

		addResultListener(result -> parent.addChild(result));
	}

	protected SchemaNodeConfigurationContext<? super N> getContext() {
		return context;
	}

	@Override
	public List<N> getOverriddenNodes() {
		return getName() == null ? Collections.emptyList()
				: getContext().overrideChild(getName(), getNodeClass());
	}

	@Override
	protected DataLoader getDataLoader() {
		return getContext().dataLoader();
	}

	@Override
	protected Namespace getNamespace() {
		return getName() != null ? getName().getNamespace()
				: getContext().namespace();
	}

	@Override
	public S name(String name) {
		return name(new QualifiedName(name, getContext().namespace()));
	}

	@Override
	public S postInputType(TypeToken<?> postInputClass) {
		assertConfigurable(this.postInputClass);
		this.postInputClass = postInputClass;

		return getThis();
	}

	protected TypeToken<?> getPostInputClass() {
		return postInputClass;
	}

	@Override
	public final S occurrences(Range<Integer> range) {
		assertConfigurable(occurrences);
		occurrences = range;

		return getThis();
	}

	public Range<Integer> getOccurrences() {
		return occurrences;
	}

	@Override
	public final S ordered(boolean ordered) {
		assertConfigurable(this.ordered);
		this.ordered = ordered;

		return getThis();
	}

	public Boolean getOrdered() {
		return ordered;
	}

}