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
import uk.co.strangeskies.modabi.impl.schema.utilities.ComplexNodeWrapper;
import uk.co.strangeskies.modabi.impl.schema.utilities.SchemaNodeConfigurationContext;
import uk.co.strangeskies.modabi.schema.ComplexNode;
import uk.co.strangeskies.modabi.schema.ComplexNodeConfigurator;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.reflection.TypeToken;

public class ComplexNodeConfiguratorImpl<T>
		extends BindingChildNodeConfiguratorImpl<ComplexNodeConfigurator<T>, ComplexNode<T>, T>
		implements ComplexNodeConfigurator<T> {
	private List<Model<? super T>> baseModel;

	private Boolean inline;

	public ComplexNodeConfiguratorImpl(SchemaNodeConfigurationContext parent) {
		super(parent);
	}

	public ComplexNodeConfiguratorImpl(ComplexNodeConfiguratorImpl<T> copy) {
		super(copy);

		this.baseModel = copy.baseModel;
		this.inline = copy.inline;
	}

	@Override
	public ComplexNodeConfigurator<T> copy() {
		return new ComplexNodeConfiguratorImpl<>(this);
	}

	@Override
	public ComplexNodeConfigurator<T> name(String name) {
		return name(new QualifiedName(name, getContext().namespace()));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V extends T> ComplexNodeConfigurator<V> model(List<? extends Model<? super V>> base) {
		baseModel = new ArrayList<>((List<? extends Model<? super T>>) base);

		return (ComplexNodeConfigurator<V>) this;
	}

	@Override
	public List<Model<? super T>> getModel() {
		return baseModel;
	}

	@Override
	public List<ComplexNode<? super T>> getOverriddenNodes() {
		List<ComplexNode<? super T>> overriddenNodes = new ArrayList<>();

		if (baseModel != null)
			for (Model<? super T> base : baseModel)
				overriddenNodes.add(ComplexNodeWrapper.wrapType(base));

		overriddenNodes.addAll(getOverriddenNodes(new TypeToken<ComplexNode<? super T>>() {}));

		return overriddenNodes;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V extends T> ComplexNodeConfigurator<V> dataType(TypeToken<? extends V> dataClass) {
		return (ComplexNodeConfigurator<V>) super.dataType(dataClass);
	}

	@Override
	protected boolean isDataContext() {
		return false;
	}

	@Override
	public ComplexNode<T> create() {
		return new ComplexNodeImpl<>(this);
	}

	@Override
	public ComplexNodeConfigurator<T> inline(boolean inline) {
		this.inline = inline;

		return this;
	}

	@Override
	public Boolean getInline() {
		return inline;
	}
}
