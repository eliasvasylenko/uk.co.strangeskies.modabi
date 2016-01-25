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
		extends
		BindingChildNodeConfiguratorImpl<ComplexNodeConfigurator<T>, ComplexNode<T>, T>
		implements ComplexNodeConfigurator<T> {
	private List<Model<? super T>> baseModel;

	private Boolean inline;

	public ComplexNodeConfiguratorImpl(
			SchemaNodeConfigurationContext<? super ComplexNode<T>> parent) {
		super(parent);
	}

	@Override
	public ComplexNodeConfigurator<T> name(String name) {
		return name(new QualifiedName(name, getContext().namespace()));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V extends T> ComplexNodeConfigurator<V> baseModel(
			List<? extends Model<? super V>> base) {
		assertConfigurable(this.baseModel);
		baseModel = new ArrayList<>((List<? extends Model<? super T>>) base);

		return (ComplexNodeConfigurator<V>) this;
	}

	public List<Model<? super T>> getBaseModel() {
		return baseModel;
	}

	@Override
	public List<ComplexNode<T>> getOverriddenNodes() {
		List<ComplexNode<T>> overriddenNodes = new ArrayList<>();

		if (baseModel != null)
			for (Model<? super T> base : baseModel)
				overriddenNodes.add(new ComplexNodeWrapper<>(base.effective()));

		overriddenNodes.addAll(super.getOverriddenNodes());

		return overriddenNodes;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V extends T> ComplexNodeConfigurator<V> dataType(
			TypeToken<? extends V> dataClass) {
		return (ComplexNodeConfigurator<V>) super.dataType(dataClass);
	}

	@Override
	protected boolean isDataContext() {
		return false;
	}

	@Override
	protected TypeToken<ComplexNode<T>> getNodeClass() {
		return new TypeToken<ComplexNode<T>>() {};
	}

	@Override
	protected ComplexNode<T> tryCreateImpl() {
		return new ComplexNodeImpl<>(this);
	}

	@Override
	public ComplexNodeConfigurator<T> inline(boolean inline) {
		assertConfigurable(this.inline);
		this.inline = inline;

		return this;
	}

	public Boolean getInline() {
		return inline;
	}
}
