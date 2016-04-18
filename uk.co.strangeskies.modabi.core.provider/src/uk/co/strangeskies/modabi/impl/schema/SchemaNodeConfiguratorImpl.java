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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import uk.co.strangeskies.modabi.Abstractness;
import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.impl.schema.utilities.ChildrenConfigurator;
import uk.co.strangeskies.modabi.impl.schema.utilities.ChildrenContainer;
import uk.co.strangeskies.modabi.impl.schema.utilities.OverrideMerge;
import uk.co.strangeskies.modabi.schema.SchemaNode;
import uk.co.strangeskies.modabi.schema.SchemaNodeConfigurator;
import uk.co.strangeskies.modabi.schema.building.ChildBuilder;
import uk.co.strangeskies.modabi.schema.building.DataLoader;
import uk.co.strangeskies.reflection.Imports;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.utilities.IdentityProperty;
import uk.co.strangeskies.utilities.factory.Configurator;
import uk.co.strangeskies.utilities.factory.InvalidBuildStateException;

public abstract class SchemaNodeConfiguratorImpl<S extends SchemaNodeConfigurator<S, N>, N extends SchemaNode<?, ?>>
		extends Configurator<N> implements SchemaNodeConfigurator<S, N> {
	private final IdentityProperty<N> finalNode;

	private ChildrenConfigurator childrenConfigurator;
	private ChildrenContainer childrenContainer;

	private boolean finalised;

	private QualifiedName name;
	private Abstractness abstractness;

	public SchemaNodeConfiguratorImpl() {
		finalNode = new IdentityProperty<>();

		finalised = false;
	}

	protected final void assertConfigurable(Object object) {
		assertConfigurable();
		if (object != null)
			throw new InvalidBuildStateException(this,
					"Property has already been configured; cannot configure with value '" + object + "'");
	}

	protected final void assertConfigurable() {
		if (finalised)
			throw new InvalidBuildStateException(this);
	}

	final void finaliseConfiguration() {
		finalised = true;

		if (childrenConfigurator == null)
			childrenConfigurator = createChildrenConfigurator();
	}

	public void finaliseChildren() {
		if (childrenContainer == null)
			childrenContainer = childrenConfigurator.create();
	}

	public ChildrenContainer getChildrenContainer() {
		return childrenContainer;
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

	@SuppressWarnings("unchecked")
	protected final S getThis() {
		return (S) this;
	}

	@Override
	public final S name(QualifiedName name) {
		assertConfigurable(this.name);
		this.name = name;

		return getThis();
	}

	public final QualifiedName getName() {
		return name;
	}

	@SuppressWarnings("unchecked")
	public N getSchemaNodeProxy() {
		Set<Class<? super N>> types = getNodeClass().getRawTypes();

		return (N) proxyNode(finalNode::get, types);
	}

	private Object proxyNode(Supplier<?> supplier, Set<? extends Class<?>> types) {
		return Proxy.newProxyInstance(getClass().getClassLoader(), types.toArray(new Class<?>[types.size()]),
				new InvocationHandler() {
					@Override
					public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
						Object node = supplier.get();
						if (node != null) {
							return method.invoke(node, args);
						} else {
							Type[] parameters = method.getParameterTypes();

							if (method.getName().equals("equals") && parameters.length == 1 && parameters[0].equals(Object.class)) {
								if (!(args[0] instanceof SchemaNode)) {
									return false;
								} else {
									return Objects.equals(getFinalName(), ((SchemaNode<?, ?>) args[0]).name());
								}
							}

							if (method.getName().equals("hashCode") && parameters.length == 0) {
								return Objects.hashCode(getFinalName());
							}

							if (method.getName().equals("getName") && parameters.length == 0) {
								return getFinalName();
							}

							if (method.getName().equals("effective") && parameters.length == 0) {
								return proxyNode(() -> finalNode.get() == null ? null : finalNode.get().effective(),
										new HashSet<>(Arrays.asList(method.getReturnType())));
							}

							throw new SchemaException(
									"Cannot invoke method '" + method + "' on node '" + getFinalName() + "' before instantiation");
						}
					}

					private Object getFinalName() {
						return getName() != null ? getName() : defaultName();
					}
				});
	}

	public QualifiedName defaultName() {
		return null;
	}

	@Override
	public final S abstractness(Abstractness abstractness) {
		assertConfigurable(this.abstractness);
		this.abstractness = abstractness;

		return getThis();
	}

	protected abstract TypeToken<N> getNodeClass();

	protected abstract DataLoader getDataLoader();

	protected abstract Namespace getNamespace();

	protected abstract Imports getImports();

	public abstract List<N> getOverriddenNodes();

	protected abstract ChildrenConfigurator createChildrenConfigurator();

	@Override
	public ChildBuilder addChild() {
		finaliseConfiguration();

		return childrenConfigurator.addChild();
	}

	protected static <S extends SchemaNode<S, ?>, C extends SchemaNodeConfiguratorImpl<?, ? extends S>> OverrideMerge<S, C> overrideMerge(
			S node, C configurator) {
		return new OverrideMerge<>(node, configurator);
	}

	protected Abstractness abstractness() {
		return abstractness;
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
