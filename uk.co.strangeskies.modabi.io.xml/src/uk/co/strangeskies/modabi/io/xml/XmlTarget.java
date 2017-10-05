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

import static java.nio.channels.Channels.newOutputStream;
import static uk.co.strangeskies.text.properties.PropertyLoader.getDefaultProperties;

import java.io.OutputStream;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.io.ModabiIOException;
import uk.co.strangeskies.modabi.io.structured.StructuredDataWriter;
import uk.co.strangeskies.modabi.io.structured.StructuredDataWriterImpl;

public class XmlTarget extends StructuredDataWriterImpl<XmlTarget> {
  private final XMLStreamWriter out;

  private final NamespaceStack namespaces;
  private boolean started;

  private QualifiedName currentChild;
  private final Map<QualifiedName, String> properties;
  private final List<String> comments;

  public XmlTarget(XMLStreamWriter out) {
    this.out = out;

    namespaces = new NamespaceStack();
    started = false;

    properties = new LinkedHashMap<>();
    comments = new ArrayList<>();

    try {
      out.setNamespaceContext(namespaces);
      out.writeStartDocument();
    } catch (XMLStreamException | FactoryConfigurationError e) {
      throw xmlException(e);
    }
  }

  public XmlTarget(OutputStream out, boolean formatted) {
    this(createXMLStreamWriter(out, formatted));
  }

  public XmlTarget(WritableByteChannel out, boolean formatted) {
    this(createXMLStreamWriter(newOutputStream(out), formatted));
  }

  private static XMLStreamWriter createXMLStreamWriter(OutputStream out, boolean formatted) {
    try {
      XMLOutputFactory factory = XMLOutputFactory.newInstance();
      XMLStreamWriter writer = factory.createXMLStreamWriter(out);
      if (formatted)
        ;// TODO writer = new IndentingXMLStreamWriter(writer);
      return writer;
    } catch (XMLStreamException | FactoryConfigurationError e) {
      throw xmlException(e);
    }
  }

  public XmlTarget(OutputStream out) {
    this(out, true);
  }

  private static ModabiIOException xmlException(Throwable e) {
    return new ModabiIOException(
        getDefaultProperties(ModabiXmlExceptionMessages.class).problemReadingFromXmlDocument(),
        e);
  }

  @Override
  public void registerDefaultNamespaceHintImpl(Namespace namespace) {
    namespaces.setDefaultNamespace(namespace);
  }

  @Override
  public void registerNamespaceHintImpl(Namespace namespace) {
    namespaces.addNamespace(namespace);
  }

  private boolean outputCurrentChild(boolean selfClosing) {
    try {
      boolean done = currentChild != null;

      if (done) {
        // write start of element
        if (selfClosing) {
          out.writeEmptyElement(currentChild.getNamespace().toHttpString(), currentChild.getName());
        } else {
          out.writeStartElement(currentChild.getNamespace().toHttpString(), currentChild.getName());
        }

        // write namespaces
        if (namespaces.getAliasSet().getDefaultNamespace() != null)
          out.writeDefaultNamespace(namespaces.getDefaultNamespace().toHttpString());
        for (Namespace namespace : namespaces.getAliasSet().getNamespaces())
          out.writeNamespace(namespaces.getNamespaceAlias(namespace), namespace.toHttpString());
        namespaces.push();

        // write properties
        for (QualifiedName property : properties.keySet())
          out.writeAttribute(
              property.getNamespace().toHttpString(),
              property.getName(),
              properties.get(property));
        properties.clear();

        // write comments
        for (String comment : comments)
          out.writeComment(comment);
        comments.clear();

        started = true;
        currentChild = null;
      } else if (!started) {
        for (String comment : comments)
          out.writeComment(comment);
        comments.clear();
      }

      return done;
    } catch (XMLStreamException e) {
      throw xmlException(e);
    }
  }

  @Override
  public void addChildImpl(QualifiedName name) {
    outputCurrentChild(false);
    currentChild = name;
  }

  @Override
  public StructuredDataWriter endChild() {
    try {
      if (!outputCurrentChild(true))
        out.writeEndElement();
    } catch (XMLStreamException e) {
      throw xmlException(e);
    }

    namespaces.pop();
    if (namespaces.isBase())
      try {
        out.writeEndDocument();
        out.flush();
      } catch (XMLStreamException e) {
        throw xmlException(e);
      }

    return this;
  }

  @Override
  public DataTarget writePropertyImpl(QualifiedName name) {
    return DataTarget.composeString(s -> properties.put(name, s), this::composeName);
  }

  @Override
  public DataTarget writeContentImpl() {
    outputCurrentChild(false);

    return DataTarget.composeString(s -> {
      try {
        out.writeCharacters(s);
      } catch (XMLStreamException e) {
        throw xmlException(e);
      }
    }, this::composeName);
  }

  private String composeName(QualifiedName name) {
    String prefix = namespaces.getNamespaceAlias(name.getNamespace());
    if (prefix.length() > 0)
      prefix += ":";
    return prefix + name.getName();
  }

  @Override
  public void commentImpl(String comment) {
    try {
      out.writeComment(comment);
    } catch (XMLStreamException e) {
      throw xmlException(e);
    }
  }
}
