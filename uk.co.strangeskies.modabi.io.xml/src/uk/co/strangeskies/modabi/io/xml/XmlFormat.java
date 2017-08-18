/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.io.xml.
 *
 * uk.co.strangeskies.modabi.io.xml is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.io.xml is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.io.xml.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi.io.xml;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.osgi.service.component.annotations.Component;

import uk.co.strangeskies.modabi.io.structured.RewritableStructuredData;
import uk.co.strangeskies.modabi.io.structured.StructuredDataFormat;
import uk.co.strangeskies.modabi.io.structured.StructuredDataReader;
import uk.co.strangeskies.modabi.io.structured.StructuredDataWriter;

@Component(property = "formatId=" + XmlFormat.XML_ID)
public class XmlFormat implements StructuredDataFormat {
	public static final String XML_ID = "xml";

	@Override
	public String getFormatId() {
		return XML_ID;
	}

	@Override
	public Set<String> getFileExtensions() {
		return new LinkedHashSet<>(Arrays.asList(XML_ID));
	}

	@Override
	public StructuredDataReader loadData(InputStream in) {
		return XmlSource.from(in);
	}

	@Override
	public StructuredDataWriter saveData(OutputStream out) {
		return new XmlTarget(out);
	}

	@Override
	public RewritableStructuredData modifyData() {
		// TODO Auto-generated method stub
		return null;
	}
}
