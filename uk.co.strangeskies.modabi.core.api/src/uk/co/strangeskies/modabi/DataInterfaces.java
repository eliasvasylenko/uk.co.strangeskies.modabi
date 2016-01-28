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

public interface DataInterfaces {
	void registerDataInterface(StructuredDataFormat handler);

	void unregisterDataInterface(StructuredDataFormat handler);

	Set<StructuredDataFormat> getRegisteredDataInterfaces();

	StructuredDataFormat getDataInterface(String id);

	default Set<StructuredDataFormat> getDataInterfaces(String extension) {
		return getRegisteredDataInterfaces().stream()
				.filter(l -> l.getFileExtensions().contains(extension))
				.collect(Collectors.toCollection(LinkedHashSet::new));
	}
}
