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

import java.util.ArrayList;
import java.util.List;

import uk.co.strangeskies.modabi.schema.ComplexNode;
import uk.co.strangeskies.modabi.schema.ComplexNodeConfigurator;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.SchemaNode;

public class ComplexNodeWrapper<T> extends BindingChildNodeWrapper<T, ComplexNode<? super T>, ComplexNode<T>>
		implements ComplexNode<T> {
	private final List<Model<? super T>> model;

	protected ComplexNodeWrapper(Model<T> component) {
		super(component);
		System.out.println("d");
		model = component.baseModel();
	}

	protected ComplexNodeWrapper(ComplexNode<? super T> base, Model<? super T> component) {
		super(base, component);
		System.out.println("e");
		model = new ArrayList<>(component.baseModel());
		model.add(0, component);

		if (!component.base().containsAll(base.base()))
			throw this.<Object>getOverrideException(ComplexNode::model, base.base(), component.base(), null);
	}

	protected ComplexNodeWrapper(ComplexNode<T> node) {
		super(node, node);
		System.out.println("f");
		model = node.model();
	}

	@SuppressWarnings("unchecked")
	@Override
	public ComplexNodeConfigurator<T> configurator() {
		ComplexNodeConfigurator<T> baseConfigurator;
		if (getBase() != null) {
			baseConfigurator = (ComplexNodeConfigurator<T>) getBase().configurator();
		} else {
			baseConfigurator = null;
		}

		return baseConfigurator.model(model);
	}

	public static <T> ComplexNodeWrapper<T> wrapType(Model<T> component) {
		return new ComplexNodeWrapper<>(component);
	}

	public static <T> ComplexNodeWrapper<? extends T> wrapNodeWithOverrideType(ComplexNode<T> node, Model<?> override) {
		/*
		 * This cast isn't strictly going to be valid according to the exact erased
		 * type, but the runtime checks in the constructor should ensure the types
		 * do fit the bounds
		 */
		@SuppressWarnings("unchecked")
		Model<? super T> castOverride = (Model<? super T>) override;
		return new ComplexNodeWrapper<>(node, castOverride);
	}

	public static <T> ComplexNodeWrapper<T> wrapNode(ComplexNode<T> node) {
		return new ComplexNodeWrapper<>(node);
	}

	@Override
	public List<Model<? super T>> model() {
		return model;
	}

	@Override
	public Boolean inline() {
		return getBase() == null ? false : getBase().inline();
	}

	@Override
	public SchemaNode<?> parent() {
		return getBase() == null ? null : getBase().parent();
	}
}
