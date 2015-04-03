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
package uk.co.strangeskies.modabi.schema.node.model.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.namespace.Namespace;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.node.SchemaNode;
import uk.co.strangeskies.modabi.schema.node.building.DataLoader;
import uk.co.strangeskies.modabi.schema.node.building.configuration.impl.BindingNodeConfiguratorImpl;
import uk.co.strangeskies.modabi.schema.node.building.configuration.impl.SchemaNodeConfiguratorImpl;
import uk.co.strangeskies.modabi.schema.node.building.configuration.impl.utilities.OverrideMerge;
import uk.co.strangeskies.modabi.schema.node.model.Model;
import uk.co.strangeskies.modabi.schema.node.model.ModelConfigurator;
import uk.co.strangeskies.reflection.TypeLiteral;
import uk.co.strangeskies.reflection.TypeToken;

public class ModelConfiguratorImpl<T> extends
		BindingNodeConfiguratorImpl<ModelConfigurator<T>, Model<T>, T> implements
		ModelConfigurator<T> {
	protected static class ModelImpl<T> extends
			BindingNodeImpl<T, Model<T>, Model.Effective<T>> implements Model<T> {
		private static class Effective<T> extends
				BindingNodeImpl.Effective<T, Model<T>, Model.Effective<T>> implements
				Model.Effective<T> {
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

			@Override
			protected QualifiedName defaultName(
					OverrideMerge<Model<T>, ? extends SchemaNodeConfiguratorImpl<?, Model<T>>> overrideMerge) {
				List<Model.Effective<? super T>> baseModel = new ArrayList<>();
				overrideMerge.configurator().getOverriddenNodes()
						.forEach(n -> baseModel.addAll(n.effective().baseModel()));
				baseModel.addAll(overrideMerge.node().baseModel().stream()
						.map(SchemaNode::effective).collect(Collectors.toSet()));

				return (baseModel == null || baseModel.size() != 1) ? null : baseModel
						.get(0).getName();
			}
		}

		private final Effective<T> effective;

		private final List<Model<? super T>> baseModel;

		public ModelImpl(ModelConfiguratorImpl<T> configurator) {
			super(configurator);

			baseModel = configurator.baseModel == null ? Collections.emptyList()
					: Collections
							.unmodifiableList(new ArrayList<>(configurator.baseModel));

			effective = new Effective<>(overrideMerge(this, configurator));
		}

		@Override
		public final List<Model<? super T>> baseModel() {
			return baseModel;
		}

		@Override
		public Effective<T> effective() {
			return effective;
		}
	}

	private final DataLoader loader;

	private List<Model<? super T>> baseModel;

	public ModelConfiguratorImpl(DataLoader loader) {
		this.loader = loader;
	}

	@Override
	protected DataLoader getDataLoader() {
		return loader;
	}

	@Override
	protected boolean isDataContext() {
		return false;
	}

	@Override
	protected Namespace getNamespace() {
		return getName().getNamespace();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V extends T> ModelConfigurator<V> baseModel(
			List<? extends Model<? super V>> base) {
		assertConfigurable(this.baseModel);
		baseModel = new ArrayList<>((List<? extends Model<? super T>>) base);

		return (ModelConfigurator<V>) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Model<T>> getOverriddenNodes() {
		return baseModel != null ? new ArrayList<>(baseModel.stream()
				.map(m -> (Model<T>) m.effective()).collect(Collectors.toList()))
				: Collections.emptyList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V extends T> ModelConfigurator<V> dataType(TypeToken<V> dataClass) {
		return (ModelConfigurator<V>) super.dataType(dataClass);
	}

	@Override
	public Model<T> tryCreate() {
		return new ModelImpl<>(this);
	}

	@Override
	protected TypeToken<Model<T>> getNodeClass() {
		return new TypeLiteral<Model<T>>() {
		};
	}
}
