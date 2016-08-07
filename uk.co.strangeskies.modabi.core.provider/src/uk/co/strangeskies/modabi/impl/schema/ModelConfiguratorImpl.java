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

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;

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
	private Boolean export;

	public ModelConfiguratorImpl(DataLoader loader, Schema schema, Imports imports) {
		this.loader = loader;
		this.schema = schema;
		this.imports = imports;
	}

	public ModelConfiguratorImpl(ModelConfiguratorImpl<T> copy) {
		super(copy);

		this.loader = copy.loader;
		this.schema = copy.schema;
		this.imports = copy.imports;

		this.baseModel = copy.baseModel;
		this.export = copy.export;
	}

	@Override
	public ModelConfigurator<T> copy() {
		return new ModelConfiguratorImpl<>(this);
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
		baseModel = new ArrayList<>((List<? extends Model<? super T>>) base);

		return (ModelConfigurator<V>) this;
	}

	public List<Model<? super T>> getBaseModel() {
		return baseModel;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Model<T>> getOverriddenNodes() {
		return baseModel != null ? new ArrayList<>(baseModel.stream().map(m -> (Model<T>) m).collect(toList()))
				: emptyList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V extends T> ModelConfigurator<V> dataType(TypeToken<? extends V> dataClass) {
		return (ModelConfigurator<V>) super.dataType(dataClass);
	}

	@Override
	public Model<T> createImpl() {
		return new ModelImpl<>(this);
	}

	@Override
	public ModelConfigurator<T> export(boolean export) {
		this.export = export;

		return getThis();
	}

	@Override
	public Boolean getExported() {
		return export;
	}
}
