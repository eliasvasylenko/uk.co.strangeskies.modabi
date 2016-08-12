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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.impl.schema.utilities.ChildrenConfigurator;
import uk.co.strangeskies.modabi.schema.ChildNode;
import uk.co.strangeskies.modabi.schema.ChildNodeConfigurator;
import uk.co.strangeskies.modabi.schema.SchemaNode;
import uk.co.strangeskies.modabi.schema.SchemaNodeConfigurator;
import uk.co.strangeskies.modabi.schema.building.ChildBuilder;
import uk.co.strangeskies.modabi.schema.building.DataLoader;
import uk.co.strangeskies.reflection.Imports;
import uk.co.strangeskies.reflection.TypeToken;

public abstract class SchemaNodeConfiguratorImpl<S extends SchemaNodeConfigurator<S, N>, N extends SchemaNode<N>>
		implements SchemaNodeConfigurator<S, N> {
	private N node;
	private RuntimeException instantiationException;

	private ChildrenConfigurator childrenConfigurator;
	private List<ChildNodeConfigurator<?, ?>> children;
	private List<ChildNode<?>> childrenResults;

	private QualifiedName name;
	private Boolean concrete;

	private boolean configurationDone;
	private boolean instantiationDone;

	public SchemaNodeConfiguratorImpl() {
		children = new ArrayList<>();

		configurationDone = false;
		instantiationDone = false;

		/*
		 * The following is not done in order to parallelize, and in fact is
		 * synchronized to behave linearly. It is done to extract a reference to the
		 * node before the constructor returns, and block the constructor method
		 * until the configurator has completed. The reason is simply to allow child
		 * nodes to reference their parent node with a final field even though they
		 * must be built before the parent. A proxy could also have been used to
		 * roughly the same effect, but this way is a little nicer.
		 * 
		 * 
		 * 
		 * 
		 * TODO neaten up synchronization using existing concurrency primitives.
		 * TODO delay instantiation to just before we start building children.
		 */
		new Thread(() -> instantiate()).start();

		synchronized (this) {
			try {
				while (node == null) {
					this.wait();
				}
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}

	protected SchemaNodeConfiguratorImpl(SchemaNodeConfiguratorImpl<S, N> copy) {
		name = copy.name;
		concrete = copy.concrete;

		children = copy.children.stream().map(SchemaNodeConfigurator::copy).collect(Collectors.toList());
	}

	@Override
	public synchronized N create() {
		configurationDone = true;
		notifyAll();
		try {
			do {
				wait();
			} while (!instantiationDone);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		if (instantiationException != null) {
			throw new RuntimeException(instantiationException);
		}

		return node;
	}

	private synchronized void instantiate() {
		try {
			createImpl();
		} catch (RuntimeException e) {
			node = null;
			instantiationException = e;
		}
		instantiationDone = true;
		notifyAll();
	}

	protected abstract N createImpl();

	protected void setResult(N node) {
		this.node = node;
		notifyAll();
		try {
			do {
				wait();
			} while (!configurationDone);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public N getResult() {
		return node;
	}

	@Override
	public List<? extends ChildNodeConfigurator<?, ?>> getChildren() {
		return children;
	}

	public List<ChildNode<?>> getChildrenResults() {
		if (childrenResults == null) {
			childrenResults = getChildrenConfigurator().create();
		}

		return childrenResults;
	}

	public ChildrenConfigurator getChildrenConfigurator() {
		if (childrenConfigurator == null) {
			childrenConfigurator = createChildrenConfigurator();
		}

		return childrenConfigurator;
	}

	@Override
	public final S name(QualifiedName name) {
		this.name = name;

		return getThis();
	}

	@Override
	public final QualifiedName getName() {
		return name;
	}

	public QualifiedName defaultName() {
		return null;
	}

	@Override
	public final S concrete(boolean concrete) {
		this.concrete = concrete;

		return getThis();
	}

	@Override
	public Boolean getConcrete() {
		return concrete;
	}

	protected void addChildConfigurator(ChildNodeConfigurator<?, ?> configurator) {
		children.add(configurator);
	}

	protected abstract DataLoader getDataLoader();

	protected abstract Namespace getNamespace();

	protected abstract Imports getImports();

	protected abstract ChildrenConfigurator createChildrenConfigurator();

	@Override
	public ChildBuilder addChild() {
		return getChildrenConfigurator().addChild();
	}

	protected boolean isChildContextAbstract() {
		return getConcrete() != null && !getConcrete();
	}

	@Override
	public String toString() {
		return getNodeType().getRawType().getSimpleName() + " configurator: " + getName();
	}

	protected TypeToken<?> parseTypeWithSubstitutedBrackets(String typeName, Imports imports) {
		return TypeToken.fromString(typeName.replace('(', '<').replace(')', '>').replace('{', '<').replace('}', '>'),
				imports);
	}

	protected abstract List<? extends SchemaNode<?>> getOverriddenAndBaseNodes();
}
