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
import uk.co.strangeskies.modabi.io.structured.BufferingStructuredDataTarget.StructuredDataTargetBufferManager;

public class BufferableStructuredDataSourceImpl extends
		StructuredDataSourceWrapper implements BufferedStructuredDataSource {
	private final StructuredDataSource wrappedComponent;
	private final StructuredDataSource buffer;
	private final StructuredDataTargetBufferManager buffers;

	public BufferableStructuredDataSourceImpl(StructuredDataSource component) {
		this(component, BufferingStructuredDataTarget.multipleBuffers());
	}

	private BufferableStructuredDataSourceImpl(StructuredDataSource component,
			StructuredDataTargetBufferManager buffers) {
		this(component, buffers, buffers.openConsumableBuffer());
	}

	private BufferableStructuredDataSourceImpl(StructuredDataSource component,
			StructuredDataTargetBufferManager buffers, StructuredDataSource buffer) {
		super(wrapComponent(component, buffer));

		this.wrappedComponent = component;
		this.buffers = buffers;
		this.buffer = buffer;
	}

	protected static StructuredDataSource wrapComponent(
			StructuredDataSource component, StructuredDataSource buffer) {
		return new StructuredDataSource() {
			@Override
			public QualifiedName startNextChild() {
				return buffer.startNextChild();
			}

			@Override
			public StructuredDataState currentState() {
				throw new AssertionError();
			}

			@Override
			public BufferedStructuredDataSource buffer() {
				throw new AssertionError();
			}

			@Override
			public StructuredDataSource split() {
				throw new AssertionError();
			}

			@Override
			public List<Integer> index() {
				throw new AssertionError();
			}

			@Override
			public DataSource readProperty(QualifiedName name) {
				return buffer.readProperty(name);
			}

			@Override
			public DataSource readContent() {
				return buffer.readContent();
			}

			@Override
			public QualifiedName peekNextChild() {
				return buffer.peekNextChild();
			}

			@Override
			public boolean hasNextChild() {
				return buffer.hasNextChild();
			}

			@Override
			public Set<QualifiedName> getProperties() {
				return buffer.getProperties();
			}

			@Override
			public Set<Namespace> getNamespaceHints() {
				return buffer.getNamespaceHints();
			}

			@Override
			public Namespace getDefaultNamespaceHint() {
				return buffer.getDefaultNamespaceHint();
			}

			@Override
			public List<String> getComments() {
				return buffer.getComments();
			}

			@Override
			public void endChild() {
				buffer.endChild();
			}
		};
	}

	@Override
	public BufferedStructuredDataSource copy() {
		throw new UnsupportedOperationException();
	}

	@Override
	public StructuredDataSource split() {
		return new BufferableStructuredDataSourceImpl(wrappedComponent, buffers,
				buffer.split());
	}

	@Override
	public BufferedStructuredDataSource buffer() {
		return new BufferableStructuredDataSourceImpl(wrappedComponent, buffers,
				buffer.buffer());
	}

	@Override
	public void reset() {
		throw new UnsupportedOperationException();
	}
}
