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

import java.util.Collections;
import java.util.List;

import uk.co.strangeskies.modabi.schema.node.AbstractComplexNode;
import uk.co.strangeskies.modabi.schema.node.Model;

public class ModelWrapper<T>
		extends
		BindingNodeWrapper<T, AbstractComplexNode.Effective<T, ?, ?>, Model.Effective<? super T>, Model<T>, Model.Effective<T>>
		implements Model.Effective<T> {
	public ModelWrapper(AbstractComplexNode.Effective<T, ?, ?> component) {
		super(component);
	}

	@Override
	public List<Model.Effective<? super T>> baseModel() {
		return Collections.unmodifiableList(getComponent().baseModel());
	}
}
