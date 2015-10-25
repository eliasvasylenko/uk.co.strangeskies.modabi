/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.io.xml.provider.
 *
 * uk.co.strangeskies.modabi.io.xml.provider is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.io.xml.provider is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.io.xml.provider.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi.io.xml;

import java.io.InputStream;

import org.osgi.service.component.annotations.Component;

import uk.co.strangeskies.modabi.io.structured.FileLoader;
import uk.co.strangeskies.modabi.io.structured.StructuredDataSource;

@Component(immediate = true, property = "id=xml")
public class XmlLoader implements FileLoader {
	@Override
	public boolean isValidForExtension(String extension) {
		return "xml".equals(extension);
	}

	@Override
	public StructuredDataSource loadFile(InputStream in) {
		return XmlSource.from(in);
	}
}
