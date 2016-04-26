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
import uk.co.strangeskies.reflection.Invokable;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.reflection.Types;

public abstract class BindingNodeWrapper<T, B extends BindingNode.Effective<? super T, ?, ?>, S extends BindingNode<T, S, E>, E extends BindingNode.Effective<T, S, E>>
		implements BindingNode.Effective<T, S, E> {
	private final BindingNode.Effective<?, ?, ?> component;
	private final B base;

	private final TypeToken<T> dataType;

	public BindingNodeWrapper(BindingNode.Effective<T, ?, ?> component) {
		this.component = component;
		base = null;
		dataType = (TypeToken<T>) component.dataType();
	}

	@SuppressWarnings("unchecked")
	public BindingNodeWrapper(B base, BindingNode.Effective<?, ?, ?> component) {
		this.component = component;
		this.base = base;

		String message = "Cannot override '" + base.name() + "' with '" + component.name() + "'";

		try {
			if (base.dataType() != null) {
				dataType = (TypeToken<T>) component.dataType().withLooseCompatibilityTo(base.dataType()).infer();
			} else {
				dataType = (TypeToken<T>) component.dataType().infer();
			}
		} catch (Exception e) {
			throw new SchemaException(message, e);
		}

		if (base.bindingStrategy() != null && base.bindingStrategy() != component.bindingStrategy())
			throw new SchemaException(message);

		if (base.unbindingStrategy() != null && base.unbindingStrategy() != component.unbindingStrategy())
			throw new SchemaException(message);

		if (base.bindingType() != null
				&& !Types.isAssignable(component.bindingType().getType(), base.bindingType().getType()))
			throw new SchemaException(message);

		if (base.unbindingType() != null
				&& !Types.isAssignable(component.unbindingType().getType(), base.unbindingType().getType()))
			throw new SchemaException(message);

		if (base.unbindingMethodName() != null && base.unbindingMethodName() != component.unbindingMethodName())
			throw new SchemaException(message);

		if (base.providedUnbindingMethodParameterNames() != null
				&& base.providedUnbindingMethodParameterNames() != component.providedUnbindingMethodParameterNames())
			throw new SchemaException(message);

		if (!component.children().containsAll(base.children()))
			throw new SchemaException(message);
	}

	public BindingNode.Effective<?, ?, ?> getComponent() {
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
	public final TypeToken<T> dataType() {
		return dataType;
	}

	@Override
	public final BindingStrategy bindingStrategy() {
		return component.bindingStrategy();
	}

	@Override
	public final TypeToken<?> bindingType() {
		return component.bindingType();
	}

	@Override
	public final UnbindingStrategy unbindingStrategy() {
		return component.unbindingStrategy();
	}

	@Override
	public final TypeToken<?> unbindingType() {
		return component.unbindingType();
	}

	@Override
	public final String unbindingMethodName() {
		return component.unbindingMethodName();
	}

	@Override
	public Boolean unbindingMethodUnchecked() {
		return component.unbindingMethodUnchecked();
	}

	@Override
	public final Invokable<?, ?> unbindingMethod() {
		return component.unbindingMethod();
	}

	@Override
	public final TypeToken<?> unbindingFactoryType() {
		return component.unbindingFactoryType();
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
	public final List<QualifiedName> providedUnbindingMethodParameterNames() {
		return component.providedUnbindingMethodParameterNames();
	}

	@Override
	public final List<DataNode.Effective<?>> providedUnbindingMethodParameters() {
		return component.providedUnbindingMethodParameters();
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
