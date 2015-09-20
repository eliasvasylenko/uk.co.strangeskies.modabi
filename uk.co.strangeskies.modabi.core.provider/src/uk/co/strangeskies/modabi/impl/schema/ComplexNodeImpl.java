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
package uk.co.strangeskies.modabi.impl.schema;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.impl.schema.utilities.OverrideMerge;
import uk.co.strangeskies.modabi.schema.AbstractComplexNode;
import uk.co.strangeskies.modabi.schema.ComplexNode;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.SchemaNode;
import uk.co.strangeskies.utilities.PropertySet;

class ComplexNodeImpl<T> extends
		BindingChildNodeImpl<T, ComplexNode<T>, ComplexNode.Effective<T>> implements
		ComplexNode<T> {
	private static class Effective<T>
			extends
			BindingChildNodeImpl.Effective<T, ComplexNode<T>, ComplexNode.Effective<T>>
			implements ComplexNode.Effective<T> {
		private final List<Model.Effective<? super T>> baseModel;

		private final boolean inline;

		protected Effective(
				OverrideMerge<ComplexNode<T>, ComplexNodeConfiguratorImpl<T>> overrideMerge) {
			super(overrideMerge);

			List<Model.Effective<? super T>> baseModel = new ArrayList<>();
			overrideMerge.configurator().getOverriddenNodes()
					.forEach(n -> baseModel.addAll(n.effective().baseModel()));
			baseModel.addAll(overrideMerge.node().baseModel().stream()
					.map(SchemaNode::effective).collect(Collectors.toSet()));
			this.baseModel = Collections.unmodifiableList(baseModel);

			this.inline = overrideMerge.getValue(ComplexNode::isInline, false);

			if (inline && isExtensible())
				throw new SchemaException("Complex node '" + getName()
						+ "' cannot be both inline and extensible");
		}

		@Override
		public List<Model.Effective<? super T>> baseModel() {
			return baseModel;
		}

		@Override
		public Boolean isInline() {
			return inline;
		}

		@SuppressWarnings("rawtypes")
		static final PropertySet<AbstractComplexNode.Effective> ABSTRACT_PROPERTY_SET = new PropertySet<>(
				AbstractComplexNode.Effective.class)
				.add(BindingNodeImpl.Effective.PROPERTY_SET)
				.add(ComplexNodeImpl.ABSTRACT_PROPERTY_SET)
				.add(AbstractComplexNode::baseModel);

		@SuppressWarnings("rawtypes")
		static final PropertySet<ComplexNode.Effective> PROPERTY_SET = new PropertySet<>(
				ComplexNode.Effective.class).add(ABSTRACT_PROPERTY_SET)
				.add(ComplexNodeImpl.PROPERTY_SET)
				.add(BindingChildNodeImpl.Effective.PROPERTY_SET);

		@SuppressWarnings("unchecked")
		@Override
		protected PropertySet<ComplexNode.Effective<T>> effectivePropertySet() {
			return (PropertySet<ComplexNode.Effective<T>>) (Object) PROPERTY_SET;
		}
	}

	private final ComplexNodeImpl.Effective<T> effective;

	private final List<Model<? super T>> baseModel;

	private final Boolean inline;

	public ComplexNodeImpl(ComplexNodeConfiguratorImpl<T> configurator) {
		super(configurator);

		baseModel = configurator.getBaseModel() == null ? Collections.emptyList()
				: Collections.unmodifiableList(new ArrayList<>(configurator
						.getBaseModel()));

		inline = configurator.getInline();

		effective = new ComplexNodeImpl.Effective<>(
				ComplexNodeConfiguratorImpl.overrideMerge(this, configurator));
	}

	@SuppressWarnings("rawtypes")
	static final PropertySet<AbstractComplexNode> ABSTRACT_PROPERTY_SET = new PropertySet<>(
			AbstractComplexNode.class).add(BindingNodeImpl.PROPERTY_SET).add(
			AbstractComplexNode::baseModel);

	@SuppressWarnings("rawtypes")
	static final PropertySet<ComplexNode> PROPERTY_SET = new PropertySet<>(
			ComplexNode.class).add(BindingChildNodeImpl.PROPERTY_SET)
			.add(ABSTRACT_PROPERTY_SET).add(AbstractComplexNode::baseModel);

	@SuppressWarnings("unchecked")
	@Override
	protected PropertySet<ComplexNode<T>> propertySet() {
		return (PropertySet<ComplexNode<T>>) (Object) PROPERTY_SET;
	}

	@Override
	public ComplexNodeImpl.Effective<T> effective() {
		return effective;
	}

	@Override
	public final List<Model<? super T>> baseModel() {
		return baseModel;
	}

	@Override
	public Boolean isInline() {
		return inline;
	}
}
