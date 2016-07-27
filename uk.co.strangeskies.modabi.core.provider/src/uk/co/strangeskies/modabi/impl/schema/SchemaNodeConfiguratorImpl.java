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

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.Abstractness;
import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.impl.schema.utilities.ChildrenConfigurator;
import uk.co.strangeskies.modabi.impl.schema.utilities.OverrideBuilder;
import uk.co.strangeskies.modabi.schema.ChildNode;
import uk.co.strangeskies.modabi.schema.SchemaNode;
import uk.co.strangeskies.modabi.schema.SchemaNodeConfigurator;
import uk.co.strangeskies.modabi.schema.building.ChildBuilder;
import uk.co.strangeskies.modabi.schema.building.DataLoader;
import uk.co.strangeskies.reflection.Imports;
import uk.co.strangeskies.reflection.TypeToken;

public abstract class SchemaNodeConfiguratorImpl<S extends SchemaNodeConfigurator<S, N>, N extends SchemaNode<N>>
		implements SchemaNodeConfigurator<S, N> {
	private N node;

	private ChildrenConfigurator childrenConfigurator;
	private List<ChildNode<?>> children;

	private QualifiedName name;
	private Abstractness abstractness;

	public SchemaNodeConfiguratorImpl() {
		this(null, null);
	}

	protected SchemaNodeConfiguratorImpl(QualifiedName name, Abstractness abstractness) {
		this.name = name;
		this.abstractness = abstractness;
	}

	protected void setResult(N node) {
		this.node = node;
	}

	protected N getResult() {
		return node;
	}

	public List<ChildNode<?>> getChildren() {
		if (children == null) {
			children = getChildrenConfigurator().create();
		}

		return children;
	}

	public ChildrenConfigurator getChildrenConfigurator() {
		if (childrenConfigurator == null)
			childrenConfigurator = createChildrenConfigurator();

		return childrenConfigurator;
	}

	@Override
	public S copy() {
		/*
		 * TODO
		 */
		throw new UnsupportedOperationException();
	}

	@Override
	public final S name(QualifiedName name) {
		this.name = name;

		return getThis();
	}

	public final QualifiedName getName() {
		return name;
	}

	public QualifiedName defaultName() {
		return null;
	}

	@Override
	public final S abstractness(Abstractness abstractness) {
		this.abstractness = abstractness;

		return getThis();
	}

	public Abstractness getAbstractness() {
		return abstractness;
	}

	protected abstract DataLoader getDataLoader();

	protected abstract Namespace getNamespace();

	protected abstract Imports getImports();

	public abstract List<? extends SchemaNode<?>> getOverriddenNodes();

	protected abstract ChildrenConfigurator createChildrenConfigurator();

	@Override
	public ChildBuilder addChild() {
		return getChildrenConfigurator().addChild();
	}

	protected boolean isChildContextAbstract() {
		return getAbstractness() != null && getAbstractness().isAtLeast(Abstractness.RESOLVED);
	}

	@Override
	public String toString() {
		return getNodeType().getRawType().getSimpleName() + " configurator: " + getName();
	}

	protected TypeToken<?> parseTypeWithSubstitutedBrackets(String typeName, Imports imports) {
		return TypeToken.fromString(typeName.replace('(', '<').replace(')', '>').replace('{', '<').replace('}', '>'),
				imports);
	}

	@SuppressWarnings("unchecked")
	public <T> Set<T> getOverridenValues(Function<N, T> valueFunction) {
		return getOverriddenNodes().stream().map(n -> valueFunction.apply((N) n)).filter(Objects::nonNull)
				.collect(Collectors.toSet());
	}

	protected <T> OverrideBuilder<T, S, N> getOverride(Function<N, T> valueFunction, Function<S, T> givenValueFunction) {
		return new OverrideBuilder<>(this, valueFunction, givenValueFunction).or();
	}

	protected <T> OverrideBuilder<T, S, N> getOverride(Function<N, T> valueFunction, T override) {
		return new OverrideBuilder<>(this, valueFunction).or(override);
	}

	protected <T> OverrideBuilder<T, S, N> getOverride(Function<N, T> valueFunction, Function<S, T> givenValueFunction,
			T override) {
		return new OverrideBuilder<>(this, valueFunction, givenValueFunction).or(override);
	}
}
