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
package uk.co.strangeskies.modabi.impl.schema.utilities;

import java.lang.reflect.Executable;
import java.util.List;

import uk.co.strangeskies.modabi.Abstractness;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.processing.BindingStrategy;
import uk.co.strangeskies.modabi.processing.UnbindingStrategy;
import uk.co.strangeskies.modabi.schema.BindingNode;
import uk.co.strangeskies.modabi.schema.ChildNode;
import uk.co.strangeskies.modabi.schema.DataNode;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.reflection.Types;

public abstract class BindingNodeWrapper<T, B extends BindingNode.Effective<? super T, ?, ?>, S extends BindingNode<T, S, E>, E extends BindingNode.Effective<T, S, E>>
		implements BindingNode.Effective<T, S, E> {
	private final BindingNode.Effective<? super T, ?, ?> component;
	private final B base;

	private final TypeToken<T> dataType;

	@SuppressWarnings("unchecked")
	public BindingNodeWrapper(BindingNode.Effective<? super T, ?, ?> component) {
		this.component = component;
		base = null;
		dataType = (TypeToken<T>) component.getDataType();
	}

	@SuppressWarnings("unchecked")
	public BindingNodeWrapper(BindingNode.Effective<? super T, ?, ?> component, B base) {
		this.component = component;
		this.base = base;

		String message = "Cannot override '" + base.name() + "' with '" + component.name() + "'";

		try {
			if (base.getDataType() != null) {
				dataType = (TypeToken<T>) component.getDataType().withLooseCompatibilityTo(base.getDataType()).infer();
			} else {
				dataType = (TypeToken<T>) component.getDataType().infer();
			}
		} catch (Exception e) {
			throw new SchemaException(message, e);
		}

		if (base.getBindingStrategy() != null && base.getBindingStrategy() != component.getBindingStrategy())
			throw new SchemaException(message);

		if (base.getUnbindingStrategy() != null && base.getUnbindingStrategy() != component.getUnbindingStrategy())
			throw new SchemaException(message);

		if (base.getBindingType() != null
				&& !Types.isAssignable(component.getBindingType().getType(), base.getBindingType().getType()))
			throw new SchemaException(message);

		if (base.getUnbindingType() != null
				&& !Types.isAssignable(component.getUnbindingType().getType(), base.getUnbindingType().getType()))
			throw new SchemaException(message);

		if (base.getUnbindingMethodName() != null && base.getUnbindingMethodName() != component.getUnbindingMethodName())
			throw new SchemaException(message);

		if (base.getProvidedUnbindingMethodParameterNames() != null
				&& base.getProvidedUnbindingMethodParameterNames() != component.getProvidedUnbindingMethodParameterNames())
			throw new SchemaException(message);

		if (!component.children().containsAll(base.children()))
			throw new SchemaException(message);
	}

	public BindingNode.Effective<? super T, ?, ?> getComponent() {
		return component;
	}

	public B getBase() {
		return base;
	}

	@Override
	public Abstractness abstractness() {
		return component.abstractness();
	}

	@Override
	public final TypeToken<T> getDataType() {
		return dataType;
	}

	@Override
	public final BindingStrategy getBindingStrategy() {
		return component.getBindingStrategy();
	}

	@Override
	public final TypeToken<?> getBindingType() {
		return component.getBindingType();
	}

	@Override
	public final UnbindingStrategy getUnbindingStrategy() {
		return component.getUnbindingStrategy();
	}

	@Override
	public final TypeToken<?> getUnbindingType() {
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
	public final TypeToken<?> getUnbindingFactoryType() {
		return component.getUnbindingFactoryType();
	}

	@Override
	public final QualifiedName name() {
		return component.name();
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

	@Override
	public String toString() {
		return name().toString();
	}

	@Override
	public BindingNode.Effective<?, ?, ?> root() {
		return component.root();
	}

	@Override
	public Schema schema() {
		return component.schema();
	}
}
