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
import java.util.function.Function;

import uk.co.strangeskies.mathematics.Range;
import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.impl.schema.utilities.OverrideBuilder;
import uk.co.strangeskies.modabi.impl.schema.utilities.SchemaNodeConfigurationContext;
import uk.co.strangeskies.modabi.schema.ChildNode;
import uk.co.strangeskies.modabi.schema.ChildNodeConfigurator;
import uk.co.strangeskies.modabi.schema.building.DataLoader;
import uk.co.strangeskies.reflection.Imports;
import uk.co.strangeskies.reflection.TypeToken;

public abstract class ChildNodeConfiguratorImpl<S extends ChildNodeConfigurator<S, N>, N extends ChildNode<N>>
		extends SchemaNodeConfiguratorImpl<S, N> implements ChildNodeConfigurator<S, N> {
	private final SchemaNodeConfigurationContext context;

	private Range<Integer> occurrences;
	private Boolean orderedOccurrences;
	private Boolean optional;
	private TypeToken<?> postInputClass;
	private List<N> overriddenNodes;

	public ChildNodeConfiguratorImpl(SchemaNodeConfigurationContext parent) {
		this.context = parent;
	}

	public ChildNodeConfiguratorImpl(ChildNodeConfiguratorImpl<S, N> copy) {
		super(copy);

		this.context = copy.context;
		this.occurrences = copy.occurrences;
		this.orderedOccurrences = copy.orderedOccurrences;
		this.optional = copy.optional;
		this.postInputClass = copy.postInputClass;
		this.overriddenNodes = copy.overriddenNodes;
	}

	@Override
	public N create() {
		N node = super.create();

		getContext().addChildResult(node);

		return node;
	}

	public SchemaNodeConfigurationContext getContext() {
		return context;
	}

	@Override
	public List<N> getOverriddenAndBaseNodes() {
		return getOverriddenNodes();
	}

	public List<N> getOverriddenNodes() {
		if (overriddenNodes == null) {
			overriddenNodes = getName() == null ? Collections.emptyList()
					: getContext().overrideChild(getName(), getNodeType());
		}
		return overriddenNodes;
	}

	@Override
	protected Imports getImports() {
		return getContext().imports();
	}

	@Override
	protected DataLoader getDataLoader() {
		return getContext().dataLoader();
	}

	@Override
	protected Namespace getNamespace() {
		return getName() != null ? getName().getNamespace() : getContext().namespace();
	}

	@Override
	public S name(String name) {
		return name(new QualifiedName(name, getContext().namespace()));
	}

	@Override
	public S optional(boolean optional) {
		this.optional = optional;

		return ChildNodeConfigurator.super.optional(optional);
	}

	@Override
	public Boolean getOptional() {
		return optional;
	}

	@Override
	public S postInputType(String postInputType) {
		return postInputType(parseTypeWithSubstitutedBrackets(postInputType, getImports()));
	}

	@Override
	public S postInputType(TypeToken<?> postInputClass) {
		this.postInputClass = postInputClass;

		return getThis();
	}

	@Override
	public TypeToken<?> getPostInputType() {
		return postInputClass;
	}

	@Override
	public final S occurrences(Range<Integer> range) {
		occurrences = range;

		return getThis();
	}

	@Override
	public Range<Integer> getOccurrences() {
		return occurrences;
	}

	@Override
	public final S orderedOccurrences(boolean ordered) {
		this.orderedOccurrences = ordered;

		return getThis();
	}

	@Override
	public Boolean getOrderedOccurrences() {
		return orderedOccurrences;
	}

	protected <T> OverrideBuilder<T, ?, ?> getOverride(Function<N, T> valueFunction, Function<S, T> givenValueFunction) {
		return new OverrideBuilder<>(this, getResult(), ChildNodeConfiguratorImpl::getOverriddenNodes, valueFunction,
				givenValueFunction.compose(SchemaNodeConfiguratorImpl::getThis));
	}
}
