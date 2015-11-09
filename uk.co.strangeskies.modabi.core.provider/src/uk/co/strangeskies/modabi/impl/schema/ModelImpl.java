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

import uk.co.strangeskies.modabi.impl.schema.utilities.OverrideMerge;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.SchemaNode;

class ModelImpl<T> extends BindingNodeImpl<T, Model<T>, Model.Effective<T>>
		implements Model<T> {
	private static class Effective<T>
			extends BindingNodeImpl.Effective<T, Model<T>, Model.Effective<T>>
			implements Model.Effective<T> {
		private final List<Model.Effective<? super T>> baseModel;

		protected Effective(
				OverrideMerge<Model<T>, ModelConfiguratorImpl<T>> overrideMerge) {
			super(overrideMerge);

			List<Model.Effective<? super T>> baseModel = new ArrayList<>();
			overrideMerge.configurator().getOverriddenNodes()
					.forEach(n -> baseModel.addAll(n.effective().baseModel()));
			baseModel.addAll(overrideMerge.node().baseModel().stream()
					.map(SchemaNode::effective).collect(Collectors.toSet()));
			this.baseModel = Collections.unmodifiableList(baseModel);
		}

		@Override
		public final List<Model.Effective<? super T>> baseModel() {
			return baseModel;
		}
	}

	private final ModelImpl.Effective<T> effective;

	private final List<Model<? super T>> baseModel;

	public ModelImpl(ModelConfiguratorImpl<T> configurator) {
		super(configurator);

		baseModel = configurator.getBaseModel() == null ? Collections.emptyList()
				: Collections
						.unmodifiableList(new ArrayList<>(configurator.getBaseModel()));

		effective = new ModelImpl.Effective<>(
				ModelConfiguratorImpl.overrideMerge(this, configurator));
	}

	@Override
	public final List<Model<? super T>> baseModel() {
		return baseModel;
	}

	@Override
	public ModelImpl.Effective<T> effective() {
		return effective;
	}
}
