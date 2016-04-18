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

import java.util.Collections;
import java.util.List;

import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.schema.BindingNode;
import uk.co.strangeskies.modabi.schema.ComplexNode;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.SchemaNode;

public class ComplexNodeWrapper<T> extends
		BindingChildNodeWrapper<T, BindingNode.Effective<? super T, ?, ?>, ComplexNode.Effective<? super T>, ComplexNode<T>, ComplexNode.Effective<T>>
		implements ComplexNode.Effective<T> {
	public ComplexNodeWrapper(BindingNode.Effective<? super T, ?, ?> component) {
		super(component);
	}

	public ComplexNodeWrapper(Model.Effective<T> component, ComplexNode.Effective<? super T> base) {
		super(component, base);

		String message = "Cannot override '" + base.name() + "' with '" + component.name() + "'";

		if (!component.baseModel().containsAll(base.model()))
			throw new SchemaException(message);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Model.Effective<? super T>> model() {
		return (List<Model.Effective<? super T>>) Collections.unmodifiableList(getComponent().base());
	}

	@Override
	public Boolean isInline() {
		return getBase() == null ? false : getBase().isInline();
	}

	@Override
	public SchemaNode.Effective<?, ?> parent() {
		return getBase() == null ? null : getBase().parent();
	}
}
