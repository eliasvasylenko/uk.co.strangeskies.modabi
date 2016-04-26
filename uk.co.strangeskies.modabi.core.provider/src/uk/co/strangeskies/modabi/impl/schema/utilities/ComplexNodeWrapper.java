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

import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.schema.ComplexNode;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.SchemaNode;

public class ComplexNodeWrapper<T>
		extends BindingChildNodeWrapper<T, ComplexNode.Effective<? super T>, ComplexNode<T>, ComplexNode.Effective<T>>
		implements ComplexNode.Effective<T> {
	private final List<Model.Effective<? super T>> model;

	protected ComplexNodeWrapper(Model.Effective<T> component) {
		super(component);
		model = component.baseModel();
	}

	protected ComplexNodeWrapper(ComplexNode.Effective<? super T> base, Model.Effective<? super T> component) {
		super(base, component);
		model = new ArrayList<>(component.baseModel());
		model.add(0, component);

		String message = "Cannot override '" + base.name() + "' with '" + component.name() + "'";

		if (!component.baseModel().containsAll(base.model()))
			throw new SchemaException(message);
	}

	protected ComplexNodeWrapper(ComplexNode.Effective<T> node) {
		super(node, node);
		model = node.model();
	}

	public static <T> ComplexNodeWrapper<T> wrapType(Model.Effective<T> component) {
		return new ComplexNodeWrapper<>(component);
	}

	public static <T> ComplexNodeWrapper<? extends T> wrapNodeWithOverrideType(ComplexNode.Effective<T> node,
			Model.Effective<?> override) {
		/*
		 * This cast isn't strictly going to be valid according to the exact erased
		 * type, but the runtime checks in the constructor should ensure the types
		 * do fit the bounds
		 */
		@SuppressWarnings("unchecked")
		Model.Effective<? super T> castOverride = (Model.Effective<? super T>) override;
		return new ComplexNodeWrapper<>(node, castOverride);
	}

	public static <T> ComplexNodeWrapper<T> wrapNode(ComplexNode.Effective<T> node) {
		return new ComplexNodeWrapper<>(node);
	}

	@Override
	public List<Model.Effective<? super T>> model() {
		return model;
	}

	@Override
	public Boolean inline() {
		return getBase() == null ? false : getBase().inline();
	}

	@Override
	public SchemaNode.Effective<?, ?> parent() {
		return getBase() == null ? null : getBase().parent();
	}
}
