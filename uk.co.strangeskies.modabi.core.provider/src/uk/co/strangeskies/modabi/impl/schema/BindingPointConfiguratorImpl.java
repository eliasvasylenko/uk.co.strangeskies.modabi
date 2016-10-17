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

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.schema.BindingPoint;
import uk.co.strangeskies.modabi.schema.BindingPointConfigurator;
import uk.co.strangeskies.modabi.schema.ChildBindingPointConfigurator;
import uk.co.strangeskies.modabi.schema.SchemaNode;
import uk.co.strangeskies.modabi.schema.SchemaNodeConfigurator;
import uk.co.strangeskies.reflection.TypeToken;

public abstract class BindingPointConfiguratorImpl<T, S extends BindingPointConfigurator<T, S>>
		implements BindingPointConfigurator<T, S> {
	private RuntimeException instantiationException;

	private QualifiedName name;
	private Boolean concrete;
	private Boolean export;

	private boolean configurationDone;
	private boolean instantiationDone;

	private BindingPoint<T> result;

	public BindingPointConfiguratorImpl() {
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
				while (result == null) {
					this.wait();
				}
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}

	protected BindingPointConfiguratorImpl(BindingPointConfigurator<T, S> copy) {
		name = copy.getName();
		concrete = copy.getConcrete();
	}

	@Override
	public synchronized BindingPoint<T> create() {
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

		return result;
	}

	private synchronized void instantiate() {
		try {
			createImpl();
		} catch (RuntimeException e) {
			result = null;
			instantiationException = e;
		}
		instantiationDone = true;
		notifyAll();
	}

	protected abstract BindingPoint<T> createImpl();

	protected void setResult(BindingPoint<T> node) {
		this.result = node;
		notifyAll();
		try {
			do {
				wait();
			} while (!configurationDone);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public BindingPoint<T> getResult() {
		return result;
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

	@Override
	public final S concrete(boolean concrete) {
		this.concrete = concrete;

		return getThis();
	}

	@Override
	public final Boolean getConcrete() {
		return concrete;
	}

	@Override
	public final S export(boolean export) {
		this.export = export;

		return getThis();
	}

	@Override
	public Boolean getExport() {
		return export;
	}

	protected boolean isChildContextAbstract() {
		return getConcrete() != null && !getConcrete();
	}

	@Override
	public TypeToken<T> getDataType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <V> ChildBindingPointConfigurator<V> dataType(TypeToken<? extends V> dataType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SchemaNodeConfigurator node() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SchemaNode getNode() {
		// TODO Auto-generated method stub
		return null;
	}
}
