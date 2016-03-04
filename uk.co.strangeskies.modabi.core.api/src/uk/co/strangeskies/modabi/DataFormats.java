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

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.io.structured.StructuredDataFormat;
import uk.co.strangeskies.utilities.Observable;

public interface DataFormats extends Observable<StructuredDataFormat> {
	void registerDataFormat(StructuredDataFormat handler);

	void unregisterDataFormat(StructuredDataFormat handler);

	Set<StructuredDataFormat> getRegistered();

	StructuredDataFormat getDataFormat(String id);

	default Set<StructuredDataFormat> getDataFormats(String extension) {
		return getRegistered().stream().filter(l -> l.getFileExtensions().contains(extension))
				.collect(Collectors.toCollection(LinkedHashSet::new));
	}
}
