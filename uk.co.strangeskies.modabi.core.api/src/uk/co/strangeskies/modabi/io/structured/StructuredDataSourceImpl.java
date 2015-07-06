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

public abstract class StructuredDataSourceImpl implements StructuredDataSource {
	private StructuredDataState currentState;

	public StructuredDataSourceImpl() {
		currentState = StructuredDataState.UNSTARTED;
	}

	@Override
	public StructuredDataState currentState() {
		return currentState;
	}

	private void enterState(StructuredDataState exitState) {
		currentState = currentState.enterState(exitState);
	}

	@Override
	public abstract QualifiedName peekNextChild();

	@Override
	public Namespace getDefaultNamespaceHint() {
		currentState().checkValid(StructuredDataState.UNSTARTED,
				StructuredDataState.ELEMENT_START);
		return getDefaultNamespaceHintImpl();
	}

	protected abstract Namespace getDefaultNamespaceHintImpl();

	@Override
	public Set<Namespace> getNamespaceHints() {
		currentState().checkValid(StructuredDataState.UNSTARTED,
				StructuredDataState.ELEMENT_START);
		return getNamespaceHintsImpl();
	}

	protected abstract Set<Namespace> getNamespaceHintsImpl();

	@Override
	public List<String> getComments() {
		currentState().checkValid(StructuredDataState.UNSTARTED,
				StructuredDataState.ELEMENT_START,
				StructuredDataState.POPULATED_ELEMENT);
		return getCommentsImpl();
	}

	protected abstract List<String> getCommentsImpl();

	@Override
	public QualifiedName startNextChild() {
		enterState(StructuredDataState.ELEMENT_START);
		return startNextChildImpl();
	}

	protected abstract QualifiedName startNextChildImpl();

	@Override
	public DataSource readProperty(QualifiedName name) {
		DataSource property = readPropertyImpl(name);
		return property == null ? null : new DataSourceDecorator(property);
	}

	protected abstract DataSource readPropertyImpl(QualifiedName name);

	@Override
	public DataSource readContent() {
		DataSource content = readContentImpl();
		return content == null ? null : new DataSourceDecorator(content);
	}

	protected abstract DataSource readContentImpl();

	@Override
	public void endChild() {
		if (depth() == 1)
			enterState(StructuredDataState.FINISHED);
		else
			enterState(StructuredDataState.POPULATED_ELEMENT);
		endChildImpl();
	}

	protected abstract void endChildImpl();
}
