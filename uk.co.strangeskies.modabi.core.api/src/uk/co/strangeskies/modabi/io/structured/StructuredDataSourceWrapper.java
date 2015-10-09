/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.core.api.
 *
 * uk.co.strangeskies.modabi.core.api is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.core.api is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.core.api.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi.io.structured;

import java.util.List;
import java.util.Set;

import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.io.DataSourceDecorator;

public class StructuredDataSourceWrapper implements StructuredDataSource {
	private StructuredDataSource component;

	private StructuredDataState currentState;

	public StructuredDataSourceWrapper(StructuredDataSource component) {
		this(component, StructuredDataState.UNSTARTED);
	}

	public StructuredDataSourceWrapper(StructuredDataSource component,
			StructuredDataState initialState) {
		this.component = component;

		currentState = initialState;
	}

	protected StructuredDataSource getComponent() {
		return component;
	}

	@Override
	public StructuredDataState currentState() {
		return currentState;
	}

	protected void setState(StructuredDataState state) {
		currentState = state;
	}

	protected void enterState(StructuredDataState exitState) {
		setState(currentState.enterState(exitState));
	}

	@Override
	public Namespace getDefaultNamespaceHint() {
		currentState().checkValid(StructuredDataState.UNSTARTED,
				StructuredDataState.ELEMENT_START);
		return getComponent().getDefaultNamespaceHint();
	}

	@Override
	public Set<Namespace> getNamespaceHints() {
		currentState().checkValid(StructuredDataState.UNSTARTED,
				StructuredDataState.ELEMENT_START);
		return getComponent().getNamespaceHints();
	}

	@Override
	public List<String> getComments() {
		currentState().checkValid(StructuredDataState.UNSTARTED,
				StructuredDataState.ELEMENT_START,
				StructuredDataState.POPULATED_ELEMENT);
		return getComponent().getComments();
	}

	@Override
	public QualifiedName startNextChild() {
		enterState(StructuredDataState.ELEMENT_START);
		return getComponent().startNextChild();
	}

	@Override
	public DataSource readProperty(QualifiedName name) {
		DataSource property = getComponent().readProperty(name);
		return property == null ? null : new DataSourceDecorator(property.copy());
	}

	@Override
	public DataSource readContent() {
		DataSource content = getComponent().readContent();
		return content == null ? null : new DataSourceDecorator(content.copy());
	}

	@Override
	public void endChild() {
		if (index().isEmpty())
			enterState(StructuredDataState.FINISHED);
		else
			enterState(StructuredDataState.POPULATED_ELEMENT);
		getComponent().endChild();
	}

	@Override
	public QualifiedName peekNextChild() {
		return getComponent().peekNextChild();
	}

	@Override
	public Set<QualifiedName> getProperties() {
		return getComponent().getProperties();
	}

	@Override
	public boolean hasNextChild() {
		return getComponent().hasNextChild();
	}

	@Override
	public List<Integer> index() {
		return getComponent().index();
	}

	@Override
	public StructuredDataSource split() {
		return new StructuredDataSourceWrapper(getComponent().split());
	}

	@Override
	public NavigableStructuredDataSource buffer() {
		return new NavigableStructuredDataSourceWrapper(getComponent().buffer());
	}
}
