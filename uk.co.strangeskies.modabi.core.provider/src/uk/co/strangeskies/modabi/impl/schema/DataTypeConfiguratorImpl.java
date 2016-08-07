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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.schema.DataType;
import uk.co.strangeskies.modabi.schema.DataTypeConfigurator;
import uk.co.strangeskies.modabi.schema.building.DataLoader;
import uk.co.strangeskies.reflection.Imports;
import uk.co.strangeskies.reflection.TypeToken;

public class DataTypeConfiguratorImpl<T> extends BindingNodeConfiguratorImpl<DataTypeConfigurator<T>, DataType<T>, T>
		implements DataTypeConfigurator<T> {
	private final DataLoader loader;
	private final Schema schema;
	private final Imports imports;

	private Boolean export;

	private DataType<? super T> baseType;

	public DataTypeConfiguratorImpl(DataLoader loader, Schema schema, Imports imports) {
		this.loader = loader;
		this.schema = schema;
		this.imports = imports;
	}

	public DataTypeConfiguratorImpl(DataTypeConfiguratorImpl<T> copy) {
		super(copy);

		this.loader = copy.loader;
		this.schema = copy.schema;
		this.imports = copy.imports;

		this.export = copy.export;

		this.baseType = copy.baseType;
	}

	@Override
	public DataTypeConfigurator<T> copy() {
		return new DataTypeConfiguratorImpl<>(this);
	}

	public Schema getSchema() {
		return schema;
	}

	@Override
	protected Imports getImports() {
		return imports;
	}

	@Override
	public DataType<T> createImpl() {
		return new DataTypeImpl<>(this);
	}

	@SuppressWarnings("unchecked")
	@Override
	public final <U extends T> DataTypeConfigurator<U> baseType(DataType<? super U> baseType) {
		this.baseType = (DataType<? super T>) baseType;

		return (DataTypeConfigurator<U>) this;
	}

	public DataType<? super T> getBaseType() {
		return baseType;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V extends T> DataTypeConfigurator<V> dataType(TypeToken<? extends V> dataClass) {
		return (DataTypeConfigurator<V>) super.dataType(dataClass);
	}

	@Override
	protected Namespace getNamespace() {
		return getName().getNamespace();
	}

	@Override
	protected boolean isDataContext() {
		return true;
	}

	@Override
	protected DataLoader getDataLoader() {
		return loader;
	}

	@Override
	public List<DataType<? super T>> getOverriddenAndBaseNodes() {
		return baseType == null ? Collections.emptyList() : Arrays.asList(baseType);
	}

	@Override
	public DataTypeConfigurator<T> export(boolean export) {
		this.export = export;

		return getThis();
	}

	@Override
	public Boolean getExported() {
		return export;
	}
}
