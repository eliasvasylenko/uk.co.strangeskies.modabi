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
package uk.co.strangeskies.modabi.impl.schema.old;

import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.List;

import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.impl.schema.BindingPointConfiguratorImpl;
import uk.co.strangeskies.modabi.schema.ComplexNode;
import uk.co.strangeskies.modabi.schema.DataLoader;
import uk.co.strangeskies.modabi.schema.ModelConfigurator;
import uk.co.strangeskies.reflection.Imports;
import uk.co.strangeskies.reflection.TypeToken;

public class ModelConfiguratorImpl<T> extends BindingPointConfiguratorImpl<T, ModelConfigurator<T>>
		implements ModelConfigurator<T> {
	private final DataLoader loader;
	private final Schema schema;
	private final Imports imports;

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
	public <V extends T> ModelConfigurator<V> baseModel(List<? extends ComplexNode<? super V>> base) {
		baseModel = new ArrayList<>((List<? extends ComplexNode<? super T>>) base);

		return (ModelConfigurator<V>) this;
	}

	public List<ComplexNode<? super T>> getBaseModel() {
		return baseModel;
	}

	@Override
	public List<ComplexNode<? super T>> getOverriddenAndBaseNodes() {
		return baseModel != null ? new ArrayList<>(baseModel) : emptyList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V extends T> ModelConfigurator<V> dataType(TypeToken<? extends V> dataClass) {
		return (ModelConfigurator<V>) super.dataType(dataClass);
	}

	@Override
	public ComplexNode<T> createImpl() {
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
