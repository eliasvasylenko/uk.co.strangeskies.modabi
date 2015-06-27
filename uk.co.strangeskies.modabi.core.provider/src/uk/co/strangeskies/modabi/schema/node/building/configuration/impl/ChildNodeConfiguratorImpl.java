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
package uk.co.strangeskies.modabi.schema.node.building.configuration.impl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import uk.co.strangeskies.modabi.namespace.Namespace;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.node.ChildNode;
import uk.co.strangeskies.modabi.schema.node.building.DataLoader;
import uk.co.strangeskies.modabi.schema.node.building.configuration.ChildNodeConfigurator;
import uk.co.strangeskies.modabi.schema.node.building.configuration.impl.utilities.SchemaNodeConfigurationContext;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.utilities.PropertySet;

public abstract class ChildNodeConfiguratorImpl<S extends ChildNodeConfigurator<S, N>, N extends ChildNode<?, ?>>
		extends SchemaNodeConfiguratorImpl<S, N> implements
		ChildNodeConfigurator<S, N> {
	@SuppressWarnings("rawtypes")
	protected static final PropertySet<ChildNode> PROPERTY_SET = new PropertySet<>(
			ChildNode.class).add(SchemaNodeImpl.PROPERTY_SET).add(
			n -> Optional.ofNullable(n.getPostInputType())
					.map(TypeToken::getAnnotatedDeclaration).orElse(null));

	protected PropertySet<? super N> propertySet() {
		return PROPERTY_SET;
	}

	@SuppressWarnings("rawtypes")
	protected static final PropertySet<ChildNode.Effective> EFFECTIVE_PROPERTY_SET = new PropertySet<>(
			ChildNode.Effective.class).add(PROPERTY_SET)
			.add(SchemaNodeImpl.Effective.PROPERTY_SET)
			.add(ChildNode.Effective::getPreInputType);

	protected PropertySet<? super N> effectivePropertySet() {
		return PROPERTY_SET;
	}

	private final SchemaNodeConfigurationContext<? super N> context;

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
		return getName() == null ? Collections.emptyList() : getContext()
				.overrideChild(getName(), getNodeClass());
	}

	@Override
	protected DataLoader getDataLoader() {
		return getContext().dataLoader();
	}

	@Override
	protected Namespace getNamespace() {
		return getName() != null ? getName().getNamespace() : getContext()
				.namespace();
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
}
