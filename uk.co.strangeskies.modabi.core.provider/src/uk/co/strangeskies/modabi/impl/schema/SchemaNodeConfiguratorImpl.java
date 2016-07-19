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

import uk.co.strangeskies.modabi.Abstractness;
import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.impl.schema.utilities.ChildrenConfigurator;
import uk.co.strangeskies.modabi.impl.schema.utilities.OverrideMerge;
import uk.co.strangeskies.modabi.schema.ChildNode;
import uk.co.strangeskies.modabi.schema.SchemaNode;
import uk.co.strangeskies.modabi.schema.SchemaNodeConfigurator;
import uk.co.strangeskies.modabi.schema.building.ChildBuilder;
import uk.co.strangeskies.modabi.schema.building.DataLoader;
import uk.co.strangeskies.reflection.Imports;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.utilities.IdentityProperty;

public abstract class SchemaNodeConfiguratorImpl<S extends SchemaNodeConfigurator<S, N>, N extends SchemaNode<N>>
		implements SchemaNodeConfigurator<S, N> {
	private final IdentityProperty<N> finalNode;

	private ChildrenConfigurator childrenConfigurator;
	private List<ChildNode<?>> children;

	private final QualifiedName name;
	private final Abstractness abstractness;

	public SchemaNodeConfiguratorImpl() {
		this(null, null);
	}

	protected SchemaNodeConfiguratorImpl(QualifiedName name, Abstractness abstractness) {
		finalNode = new IdentityProperty<>();

		this.name = name;
		this.abstractness = abstractness;
	}

	final void finaliseConfiguration() {
		if (childrenConfigurator == null)
			childrenConfigurator = createChildrenConfigurator();
	}

	public void finaliseChildren() {
		if (children == null) {
			finaliseConfiguration();
			children = childrenConfigurator.create();
		}
	}

	public List<ChildNode<?>> getChildren() {
		return children;
	}

	public ChildrenConfigurator getChildrenConfigurator() {
		return childrenConfigurator;
	}

	@Override
	protected final N tryCreate() {
		finalNode.set(tryCreateImpl());
		return finalNode.get();
	}

	protected abstract N tryCreateImpl();

	@Override
	@SuppressWarnings("unchecked")
	public final S getThis() {
		return SchemaNodeConfigurator.super.getThis();
	}

	@Override
	public S copy() {
		return getThis();
	}

	@Override
	public final S name(QualifiedName name) {
		if (Objects.equals(this.name, name))
			return getThis();
		else {
			/*
			 * TODO here go ALL checks to make sure new settings will be valid
			 */
			return copyImpl().setNameImpl(name);
		}
	}

	public final QualifiedName getName() {
		return name;
	}

	public N getDeclaredNode() {
		return null; // TODO return a partial node implementation over declared
									// values
	}

	public QualifiedName defaultName() {
		return null;
	}

	@Override
	public final S abstractness(Abstractness abstractness) {
		if (Objects.equals(this.abstractness, abstractness))
			return getThis();
		else {
			return copyImpl().setAbstractnessImpl(abstractness);
		}
	}

	protected Abstractness abstractness() {
		return abstractness;
	}

	protected abstract TypeToken<N> getNodeClass();

	protected abstract DataLoader getDataLoader();

	protected abstract Namespace getNamespace();

	protected abstract Imports getImports();

	public abstract List<? extends SchemaNode<?>> getOverriddenNodes();

	protected abstract ChildrenConfigurator createChildrenConfigurator();

	@Override
	public ChildBuilder addChild() {
		finaliseConfiguration();

		return childrenConfigurator.addChild();
	}

	protected static <S extends SchemaNode<S>, C extends SchemaNodeConfiguratorImpl<?, ? extends S>> OverrideMerge<S, C> overrideMerge(
			S node, C configurator) {
		return new OverrideMerge<>(node, configurator);
	}

	protected boolean isChildContextAbstract() {
		return abstractness() != null && abstractness().isAtLeast(Abstractness.RESOLVED);
	}

	@Override
	public String toString() {
		return getNodeClass().getRawType().getSimpleName() + " configurator: " + getName();
	}

	protected TypeToken<?> parseTypeWithSubstitutedBrackets(String typeName, Imports imports) {
		return TypeToken.fromString(typeName.replace('(', '<').replace(')', '>').replace('{', '<').replace('}', '>'),
				imports);
	}
}
