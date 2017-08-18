/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
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

public class BufferableStructuredDataSourceImpl extends StructuredDataReaderWrapper
		implements NavigableStructuredDataReader {
	private final StructuredDataReader wrappedComponent;
	private final StructuredDataReader buffer;
	private final StructuredDataBuffer buffers;

	public BufferableStructuredDataSourceImpl(StructuredDataReader component) {
		this(component, StructuredDataBuffer.multipleBuffers());
	}

	private BufferableStructuredDataSourceImpl(StructuredDataReader component, StructuredDataBuffer buffers) {
		this(component, buffers, buffers.openBuffer());
	}

	private BufferableStructuredDataSourceImpl(StructuredDataReader component, StructuredDataBuffer buffers,
			StructuredDataReader buffer) {
		super(wrapComponent(component, buffers, buffer));

		this.wrappedComponent = component;
		this.buffers = buffers;
		this.buffer = buffer;
	}

	protected static StructuredDataReader wrapComponent(StructuredDataReader component, StructuredDataBuffer buffers,
			StructuredDataReader buffer) {
		return new StructuredDataReader() {
			@Override
			public QualifiedName readNextChild() {
				if (buffer.index().equals(component.index())) {
					QualifiedName child = component.readNextChild();
					if (child != null) {
						buffers.addChild(child);
						component.pipeDataAtChild(buffers);
					}
				}

				return buffer.readNextChild();
			}

			@Override
			public StructuredDataState currentState() {
				return buffer.currentState();
			}

			@Override
			public NavigableStructuredDataReader buffer() {
				throw new AssertionError();
			}

			@Override
			public StructuredDataReader split() {
				throw new AssertionError();
			}

			@Override
			public List<Integer> index() {
				return buffer.index();
			}

			@Override
			public String readProperty(QualifiedName name) {
				return buffer.readProperty(name);
			}

			@Override
			public String readContent() {
				return buffer.readContent();
			}

			@Override
			public QualifiedName peekNextChild() {
				if (buffer.index().equals(component.index())) {
					return component.peekNextChild();
				} else {
					return buffer.readNextChild();
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
			public StructuredDataReader endChild() {
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
	public NavigableStructuredDataReader copy() {
		return buffer();
	}

	@Override
	public StructuredDataReader split() {
		return new BufferableStructuredDataSourceImpl(wrappedComponent, buffers, buffer.split());
	}

	@Override
	public NavigableStructuredDataReader buffer() {
		return new BufferableStructuredDataSourceImpl(wrappedComponent, buffers, buffer.buffer());
	}

	@Override
	public void reset() {
		throw new UnsupportedOperationException();
	}
}
