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

import uk.co.strangeskies.modabi.io.structured.BufferingStructuredDataTarget.StructuredDataTargetBufferManager;

public class BufferableStructuredDataSourceImpl extends
		StructuredDataSourceWrapper {
	private final StructuredDataTargetBufferManager buffers = BufferingStructuredDataTarget
			.multipleBuffers();

	public BufferableStructuredDataSourceImpl(StructuredDataSource component) {
		super(component);
	}

	@Override
	public StructuredDataSource split() {
		// TODO Auto-generated method stub

		StructuredDataSource buffer = buffers.openConsumableBuffer();

		return null;
	}

	@Override
	public BufferedStructuredDataSource buffer() {
		// TODO Auto-generated method stub

		BufferedStructuredDataSource buffer = buffers.openBuffer();

		return null;
	}
}
