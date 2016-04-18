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
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.impl.schema.utilities.OverrideMerge;
import uk.co.strangeskies.modabi.schema.BindingNode;
import uk.co.strangeskies.modabi.schema.ComplexNode;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.SchemaNode;

class ComplexNodeImpl<T> extends BindingChildNodeImpl<T, ComplexNode<T>, ComplexNode.Effective<T>>
		implements ComplexNode<T> {
	private static class Effective<T> extends BindingChildNodeImpl.Effective<T, ComplexNode<T>, ComplexNode.Effective<T>>
			implements ComplexNode.Effective<T> {
		private final List<Model.Effective<? super T>> baseModel;

		private final boolean inline;

		protected Effective(OverrideMerge<ComplexNode<T>, ComplexNodeConfiguratorImpl<T>> overrideMerge) {
			super(overrideMerge);

			List<Model.Effective<? super T>> baseModel = new ArrayList<>();
			overrideMerge.configurator().getOverriddenNodes().forEach(n -> baseModel.addAll(n.effective().model()));
			baseModel.addAll(overrideMerge.node().model().stream().map(SchemaNode::effective).collect(Collectors.toSet()));
			this.baseModel = Collections.unmodifiableList(baseModel);

			Boolean inline = overrideMerge.getOverride(ComplexNode::isInline).orDefault(false).get();
			this.inline = inline != null && inline;

			if (this.inline && isExtensible() != null && isExtensible())
				throw new SchemaException("Complex node '" + name() + "' cannot be both inline and extensible");
		}

		@Override
		public List<Model.Effective<? super T>> model() {
			return baseModel;
		}

		@Override
		public Boolean isInline() {
			return inline;
		}

		@Override
		public BindingNode.Effective<?, ?, ?> root() {
			return parent().root();
		}
	}

	private final ComplexNodeImpl.Effective<T> effective;

	private final List<Model<? super T>> baseModel;

	private final Boolean inline;

	public ComplexNodeImpl(ComplexNodeConfiguratorImpl<T> configurator) {
		super(configurator);

		baseModel = configurator.getBaseModel() == null ? Collections.emptyList()
				: Collections.unmodifiableList(new ArrayList<>(configurator.getBaseModel()));

		inline = configurator.getInline();

		effective = new ComplexNodeImpl.Effective<>(ComplexNodeConfiguratorImpl.overrideMerge(this, configurator));
	}

	@Override
	public ComplexNodeImpl.Effective<T> effective() {
		return effective;
	}

	@Override
	public final List<Model<? super T>> model() {
		return baseModel;
	}

	@Override
	public Boolean isInline() {
		return inline;
	}

	@Override
	public BindingNode<?, ?, ?> root() {
		return parent().root();
	}
}
