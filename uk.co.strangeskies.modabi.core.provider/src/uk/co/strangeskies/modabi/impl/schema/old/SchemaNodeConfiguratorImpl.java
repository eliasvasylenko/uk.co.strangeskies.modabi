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
package uk.co.strangeskies.modabi.impl.schema.old;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.impl.schema.utilities.ChildrenConfigurator;
import uk.co.strangeskies.modabi.impl.schema.utilities.ChildrenConfiguratorImpl;
import uk.co.strangeskies.modabi.impl.schema.utilities.SchemaNodeConfigurationContext;
import uk.co.strangeskies.modabi.schema.ChildBindingPoint;
import uk.co.strangeskies.modabi.schema.ChildBindingPointConfigurator;
import uk.co.strangeskies.modabi.schema.InputInitializerConfigurator;
import uk.co.strangeskies.modabi.schema.OutputInitializerConfigurator;
import uk.co.strangeskies.modabi.schema.SchemaNode;
import uk.co.strangeskies.modabi.schema.SchemaNodeConfigurator;
import uk.co.strangeskies.reflection.Imports;
import uk.co.strangeskies.reflection.TypeToken;

public class SchemaNodeConfiguratorImpl implements SchemaNodeConfigurator {
	private final SchemaNodeConfigurationContext context;

	private RuntimeException instantiationException;

	private ChildrenConfigurator childrenConfigurator;
	private List<ChildBindingPointConfigurator<?>> children;
	private List<ChildBindingPoint<?>> childrenResults;

	private boolean configurationDone;
	private boolean instantiationDone;

	public SchemaNodeConfiguratorImpl(SchemaNodeConfigurationContext context) {
		this.context = context;

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

	protected SchemaNodeConfigurationContext getContext() {
		return context;
	}

	protected SchemaNodeConfiguratorImpl(SchemaNodeConfiguratorImpl copy) {
		context = copy.context;

		children = copy.children.stream().map(ChildBindingPointConfigurator::copy).collect(Collectors.toList());
	}

	@Override
	public synchronized SchemaNode create() {
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
			new SchemaNodeImpl(this);
		} catch (RuntimeException e) {
			node = null;
			instantiationException = e;
		}
		instantiationDone = true;
		notifyAll();
	}

	protected void setResult(SchemaNode node) {
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

	@Override
	public List<ChildBindingPointConfigurator<?>> getChildBindingPoints() {
		return children;
	}

	public List<ChildBindingPoint<?>> getChildrenResults() {
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

	private ChildrenConfigurator createChildrenConfigurator() {
		return new ChildrenConfiguratorImpl(context);
	}

	@Override
	public final SchemaNodeConfigurator name(QualifiedName name) {
		this.name = name;

		return getThis();
	}

	protected void addChildConfigurator(ChildBindingPointConfigurator<?> configurator) {
		children.add(configurator);
	}

	@Override
	public ChildBindingPointConfigurator<?> addChildBindingPoint() {
		return getChildrenConfigurator().addChild();
	}

	@Override
	public String toString() {
		return "Schema node configurator: " + getName(); // TODO raw string...
	}

	protected TypeToken<?> parseTypeWithSubstitutedBrackets(String typeName, Imports imports) {
		return TypeToken.fromString(typeName.replace('(', '<').replace(')', '>').replace('{', '<').replace('}', '>'),
				imports);
	}

	@Override
	public SchemaNodeConfigurator copy() {
		return new SchemaNodeConfiguratorImpl(this);
	}

	@Override
	public InputInitializerConfigurator initializeInput() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OutputInitializerConfigurator<?> initializeOutput() {
		// TODO Auto-generated method stub
		return null;
	}
}
