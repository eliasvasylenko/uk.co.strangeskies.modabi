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
package uk.co.strangeskies.modabi.schema.node.wrapping.impl;

import java.lang.reflect.Executable;
import java.lang.reflect.Type;
import java.util.List;

import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.management.binding.BindingStrategy;
import uk.co.strangeskies.modabi.schema.management.unbinding.UnbindingStrategy;
import uk.co.strangeskies.modabi.schema.node.BindingNode;
import uk.co.strangeskies.modabi.schema.node.ChildNode;
import uk.co.strangeskies.modabi.schema.node.DataNode;
import uk.co.strangeskies.reflection.TypeToken;

public abstract class BindingNodeWrapper<T, C extends BindingNode.Effective<? super T, ?, ?>, B extends BindingNode.Effective<? super T, ?, ?>, S extends BindingNode<T, S, E>, E extends BindingNode.Effective<T, S, E>>
		implements BindingNode.Effective<T, S, E> {
	private final C component;
	private final B base;

	public BindingNodeWrapper(C component) {
		this.component = component;
		base = null;
	}

	public BindingNodeWrapper(C component, B base) {
		this.component = component;
		this.base = base;

		String message = "Cannot override '" + base.getName() + "' with '"
				+ component.getName() + "'.";

		if (base.getDataType() != null
				&& !TypeToken.of(base.getDataType().getType()).isAssignableFrom(
						component.getDataType().getType()))
			throw new SchemaException(message);

		if (base.getBindingStrategy() != null
				&& base.getBindingStrategy() != component.getBindingStrategy())
			throw new SchemaException(message);

		if (base.getUnbindingStrategy() != null
				&& base.getUnbindingStrategy() != component.getUnbindingStrategy())
			throw new SchemaException(message);

		if (base.getBindingType() != null
				&& !TypeToken.of(base.getBindingType()).isAssignableFrom(
						component.getBindingType()))
			throw new SchemaException(message);

		if (base.getUnbindingType() != null
				&& !TypeToken.of(base.getUnbindingType()).isAssignableFrom(
						component.getUnbindingType()))
			throw new SchemaException(message);

		if (base.getUnbindingMethodName() != null
				&& base.getUnbindingMethodName() != component.getUnbindingMethodName())
			throw new SchemaException(message);

		if (base.getProvidedUnbindingMethodParameterNames() != null
				&& base.getProvidedUnbindingMethodParameterNames() != component
						.getProvidedUnbindingMethodParameterNames())
			throw new SchemaException(message);

		if (!component.children().containsAll(base.children()))
			throw new SchemaException(message);
	}

	protected C getComponent() {
		return component;
	}

	protected B getBase() {
		return base;
	}

	@Override
	public final Boolean isAbstract() {
		return component.isAbstract();
	}

	@SuppressWarnings("unchecked")
	@Override
	public final TypeToken<T> getDataType() {
		return (TypeToken<T>) component.getDataType();
	}

	@Override
	public final BindingStrategy getBindingStrategy() {
		return component.getBindingStrategy();
	}

	@Override
	public final Type getBindingType() {
		return component.getBindingType();
	}

	@Override
	public final UnbindingStrategy getUnbindingStrategy() {
		return component.getUnbindingStrategy();
	}

	@Override
	public final Type getUnbindingType() {
		return component.getUnbindingType();
	}

	@Override
	public final String getUnbindingMethodName() {
		return component.getUnbindingMethodName();
	}

	@Override
	public Boolean isUnbindingMethodUnchecked() {
		return component.isUnbindingMethodUnchecked();
	}

	@Override
	public final Executable getUnbindingMethod() {
		return component.getUnbindingMethod();
	}

	@Override
	public final Type getUnbindingFactoryType() {
		return component.getUnbindingFactoryType();
	}

	@Override
	public final QualifiedName getName() {
		return component.getName();
	}

	@Override
	public final List<ChildNode.Effective<?, ?>> children() {
		return component.children();
	}

	@Override
	public final List<QualifiedName> getProvidedUnbindingMethodParameterNames() {
		return component.getProvidedUnbindingMethodParameterNames();
	}

	@Override
	public final List<DataNode.Effective<?>> getProvidedUnbindingMethodParameters() {
		return component.getProvidedUnbindingMethodParameters();
	}

	@SuppressWarnings("unchecked")
	@Override
	public S source() {
		return (S) this;
	}
}
