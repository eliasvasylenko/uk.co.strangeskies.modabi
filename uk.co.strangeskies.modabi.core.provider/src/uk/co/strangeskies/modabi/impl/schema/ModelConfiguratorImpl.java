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

import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.ModelConfigurator;
import uk.co.strangeskies.modabi.schema.building.DataLoader;
import uk.co.strangeskies.reflection.Imports;
import uk.co.strangeskies.reflection.TypeToken;

public class ModelConfiguratorImpl<T> extends BindingNodeConfiguratorImpl<ModelConfigurator<T>, Model<T>, T>
		implements ModelConfigurator<T> {
	private final DataLoader loader;
	private final Schema schema;
	private final Imports imports;

	private List<Model<? super T>> baseModel;

	public ModelConfiguratorImpl(DataLoader loader, Schema schema, Imports imports) {
		this.loader = loader;
		this.schema = schema;
		this.imports = imports;
	}

	public Schema getSchema() {
		return schema;
	}

	@Override
	public QualifiedName defaultName() {
		return (baseModel == null || baseModel.size() != 1) ? null : baseModel.get(0).name();
	}

	@Override
	protected DataLoader getDataLoader() {
		return loader;
	}
	
	@Override
	protected Imports getImports() {
		return imports;
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
	public <V extends T> ModelConfigurator<V> baseModel(List<? extends Model<? super V>> base) {
		assertConfigurable(this.baseModel);
		baseModel = new ArrayList<>((List<? extends Model<? super T>>) base);

		return (ModelConfigurator<V>) this;
	}

	public List<Model<? super T>> getBaseModel() {
		return baseModel;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Model<T>> getOverriddenNodes() {
		return baseModel != null
				? new ArrayList<>(baseModel.stream().map(m -> (Model<T>) m.effective()).collect(Collectors.toList()))
				: Collections.emptyList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V extends T> ModelConfigurator<V> dataType(TypeToken<? extends V> dataClass) {
		return (ModelConfigurator<V>) super.dataType(dataClass);
	}

	@Override
	public Model<T> tryCreateImpl() {
		return new ModelImpl<>(this);
	}

	@Override
	protected TypeToken<Model<T>> getNodeClass() {
		return new TypeToken<Model<T>>() {};
	}
}
