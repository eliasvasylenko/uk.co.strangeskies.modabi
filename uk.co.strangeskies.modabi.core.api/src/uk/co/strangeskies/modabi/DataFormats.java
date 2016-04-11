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
package uk.co.strangeskies.modabi;

import uk.co.strangeskies.modabi.io.structured.StructuredDataFormat;

public class DataFormats extends NamedSet<DataFormats, String, StructuredDataFormat> {
	public DataFormats() {
		this(null);
	}

	public DataFormats(DataFormats parent) {
		super(StructuredDataFormat::getFormatId, parent);
	}

	@Override
	public DataFormats nestChildScope() {
		return new DataFormats(this);
	}

	@Override
	public DataFormats copy() {
		DataFormats copy = new DataFormats();
		copy.addAll(this);
		return copy;
	}
}
