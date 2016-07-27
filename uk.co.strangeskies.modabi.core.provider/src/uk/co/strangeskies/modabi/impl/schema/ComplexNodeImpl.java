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
import java.util.Collections;
import java.util.List;

import uk.co.strangeskies.modabi.ModabiException;
import uk.co.strangeskies.modabi.schema.BindingNode;
import uk.co.strangeskies.modabi.schema.ComplexNode;
import uk.co.strangeskies.modabi.schema.ComplexNodeConfigurator;
import uk.co.strangeskies.modabi.schema.Model;

class ComplexNodeImpl<T> extends BindingChildNodeImpl<T, ComplexNode<T>> implements ComplexNode<T> {
	private final List<Model<? super T>> baseModel;

	private final boolean inline;

	protected ComplexNodeImpl(ComplexNodeConfiguratorImpl<T> configurator) {
		super(configurator);

		List<Model<? super T>> baseModel = new ArrayList<>();
		configurator.getOverriddenNodes().forEach(n -> baseModel.addAll(n.model()));
		baseModel.addAll(configurator.getModel());
		this.baseModel = Collections.unmodifiableList(baseModel);

		Boolean inline = configurator.getOverride(ComplexNode::inline, ComplexNodeConfigurator::getInline).orDefault(false)
				.get();
		this.inline = inline != null && inline;

		if (this.inline && extensible() != null && extensible())
			throw new ModabiException(t -> t.cannotBeInlineExtensible(name()));
	}

	@SuppressWarnings("unchecked")
	@Override
	public ComplexNodeConfigurator<T> configurator() {
		return (ComplexNodeConfigurator<T>) super.configurator();
	}

	@Override
	public List<Model<? super T>> model() {
		return baseModel;
	}

	@Override
	public Boolean inline() {
		return inline;
	}

	@Override
	public BindingNode<?, ?> root() {
		return parent().root();
	}
}
