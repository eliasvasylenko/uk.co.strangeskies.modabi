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
import java.util.List;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.ValueResolution;
import uk.co.strangeskies.modabi.impl.schema.utilities.DataNodeWrapper;
import uk.co.strangeskies.modabi.impl.schema.utilities.SchemaNodeConfigurationContext;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.schema.DataNode;
import uk.co.strangeskies.modabi.schema.DataNode.Format;
import uk.co.strangeskies.modabi.schema.DataNodeConfigurator;
import uk.co.strangeskies.modabi.schema.DataType;
import uk.co.strangeskies.reflection.TypeToken;

public class DataNodeConfiguratorImpl<T> extends
		BindingChildNodeConfiguratorImpl<DataNodeConfigurator<T>, DataNode<T>, T> implements DataNodeConfigurator<T> {
	private Format format;

	private DataType<T> type;
	private DataSource providedBufferedValue;
	private ValueResolution resolution;

	public DataNodeConfiguratorImpl(SchemaNodeConfigurationContext parent) {
		super(parent);
		
		System.out.println("?");
	}

	@Override
	public DataNodeConfigurator<T> name(String name) {
		return name(new QualifiedName(name, getContext().namespace()));
	}

	@Override
	public QualifiedName defaultName() {
		return type == null ? null : type.effective().name();
	}

	@SuppressWarnings("unchecked")
	@Override
	public final <U extends T> DataNodeConfigurator<U> dataType(TypeToken<? extends U> dataClass) {
		return (DataNodeConfigurator<U>) super.dataType(dataClass);
	}

	@SuppressWarnings("unchecked")
	@Override
	public final <U extends T> DataNodeConfigurator<U> type(DataType<U> type) {
		assertConfigurable(this.type);
		this.type = (DataType<T>) type;

		return (DataNodeConfigurator<U>) getThis();
	}

	public DataType<T> getType() {
		return type;
	}

	@Override
	public List<DataNode<? super T>> getOverriddenNodes() {
		List<DataNode<? super T>> overriddenNodes = new ArrayList<>();

		if (type != null)
			overriddenNodes.add(DataNodeWrapper.wrapType(type.effective()));

		overriddenNodes.addAll(getOverriddenNodes(new TypeToken<DataNode<? super T>>() {}));

		return overriddenNodes;
	}

	@Override
	protected boolean isDataContext() {
		return true;
	}

	@Override
	public DataNodeConfigurator<T> provideValue(DataSource dataSource) {
		assertConfigurable(providedBufferedValue);
		providedBufferedValue = dataSource;
		
		System.out.println("    Providing VALUE:::   " + dataSource);

		return this;
	}

	public DataSource getProvidedBufferedValue() {
		return providedBufferedValue;
	}

	@Override
	public DataNodeConfigurator<T> valueResolution(ValueResolution valueResolution) {
		assertConfigurable(this.resolution);
		this.resolution = valueResolution;

		return this;
	}

	public ValueResolution getResolution() {
		return resolution;
	}

	@Override
	public final DataNodeConfigurator<T> format(Format format) {
		assertConfigurable(this.format);
		this.format = format;

		return this;
	}

	public Format getFormat() {
		return format;
	}

	@Override
	protected final TypeToken<DataNode<T>> getNodeClass() {
		return new TypeToken<DataNode<T>>() {};
	}

	@Override
	protected final DataNode<T> tryCreateImpl() {
		return new DataNodeImpl<>(this);
	}
}
