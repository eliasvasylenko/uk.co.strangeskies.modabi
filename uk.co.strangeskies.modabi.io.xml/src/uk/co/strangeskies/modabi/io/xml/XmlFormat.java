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

import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.stream.Stream;

import org.osgi.service.component.annotations.Component;

import uk.co.strangeskies.modabi.io.structured.DataFormat;
import uk.co.strangeskies.modabi.io.structured.NavigableStructuredDataWriter;
import uk.co.strangeskies.modabi.io.structured.StructuredDataReader;
import uk.co.strangeskies.modabi.io.structured.StructuredDataWriter;

@Component(property = "formatId=" + XmlFormat.XML_ID)
public class XmlFormat implements DataFormat {
  public static final String XML_ID = "xml";

  @Override
  public String getFormatId() {
    return XML_ID;
  }

  @Override
  public Stream<String> getFileExtensions() {
    return Stream.of(XML_ID);
  }

  @Override
  public StructuredDataReader readData(ReadableByteChannel channel) {
    return XmlSource.from(channel);
  }

  @Override
  public StructuredDataWriter writeData(WritableByteChannel channel) {
    return new XmlTarget(channel, false);
  }

  @Override
  public NavigableStructuredDataWriter modifyData(SeekableByteChannel channel) {
    // TODO Auto-generated method stub
    return null;
  }
}
