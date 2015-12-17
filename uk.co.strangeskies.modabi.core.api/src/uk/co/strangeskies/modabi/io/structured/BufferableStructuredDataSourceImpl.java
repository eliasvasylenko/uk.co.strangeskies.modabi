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

public class BufferableStructuredDataSourceImpl extends
		StructuredDataSourceWrapper implements NavigableStructuredDataSource {
	private final StructuredDataSource wrappedComponent;
	private final StructuredDataSource buffer;
	private final StructuredDataBuffer buffers;

	public BufferableStructuredDataSourceImpl(StructuredDataSource component) {
		this(component, StructuredDataBuffer.multipleBuffers());
	}

	private BufferableStructuredDataSourceImpl(StructuredDataSource component,
			StructuredDataBuffer buffers) {
		this(component, buffers, buffers.openBuffer());
	}

	private BufferableStructuredDataSourceImpl(StructuredDataSource component,
			StructuredDataBuffer buffers, StructuredDataSource buffer) {
		super(wrapComponent(component, buffers, buffer));

		this.wrappedComponent = component;
		this.buffers = buffers;
		this.buffer = buffer;
	}

	protected static StructuredDataSource wrapComponent(
			StructuredDataSource component, StructuredDataBuffer buffers,
			StructuredDataSource buffer) {
		return new StructuredDataSource() {
			@Override
			public QualifiedName startNextChild() {
				if (buffer.index().equals(component.index())) {
					QualifiedName child = component.startNextChild();
					if (child != null) {
						buffers.addChild(child);
						component.pipeDataAtChild(buffers);
					}
				}

				return buffer.startNextChild();
			}

			@Override
			public StructuredDataState currentState() {
				return buffer.currentState();
			}

			@Override
			public NavigableStructuredDataSource buffer() {
				throw new AssertionError();
			}

			@Override
			public StructuredDataSource split() {
				throw new AssertionError();
			}

			@Override
			public List<Integer> index() {
				return buffer.index();
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
				if (buffer.index().equals(component.index())) {
					return component.peekNextChild();
				} else {
					return buffer.startNextChild();
				}
			}

			@Override
			public boolean hasNextChild() {
				if (buffer.index().equals(component.index())) {
					return component.hasNextChild();
				} else {
					return buffer.hasNextChild();
				}
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
			public StructuredDataSource endChild() {
				if (buffer.index().equals(component.index())) {
					component.endChild();
					buffers.endChild();
				}

				buffer.endChild();
				
				return this;
			}
		};
	}

	@Override
	public NavigableStructuredDataSource copy() {
		throw new UnsupportedOperationException();
	}

	@Override
	public StructuredDataSource split() {
		return new BufferableStructuredDataSourceImpl(wrappedComponent, buffers,
				buffer.split());
	}

	@Override
	public NavigableStructuredDataSource buffer() {
		return new BufferableStructuredDataSourceImpl(wrappedComponent, buffers,
				buffer.buffer());
	}

	@Override
	public void reset() {
		throw new UnsupportedOperationException();
	}
}
