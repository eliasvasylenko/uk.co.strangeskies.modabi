/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.io.json.
 *
 * uk.co.strangeskies.modabi.io.json is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.io.json is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.io.json.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi.io.json.impl;

import java.util.List;
import java.util.Set;

import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.io.structured.NavigableStructuredDataReader;
import uk.co.strangeskies.modabi.io.structured.StructuredDataReader;
import uk.co.strangeskies.modabi.io.structured.StructuredDataState;

public class JsonSource implements StructuredDataReader {
	@Override
	public StructuredDataState currentState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Namespace getDefaultNamespaceHint() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Namespace> getNamespaceHints() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getComments() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public QualifiedName readNextChild() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public QualifiedName peekNextChild() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<QualifiedName> getProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataSource readProperty(QualifiedName name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataSource readContent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasNextChild() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public StructuredDataReader endChild() {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public List<Integer> index() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StructuredDataReader split() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NavigableStructuredDataReader buffer() {
		// TODO Auto-generated method stub
		return null;
	}
}
