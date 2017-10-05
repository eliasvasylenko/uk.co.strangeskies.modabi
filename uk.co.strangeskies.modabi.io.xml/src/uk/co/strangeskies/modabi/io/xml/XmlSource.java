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

import static java.nio.channels.Channels.newInputStream;
import static uk.co.strangeskies.text.properties.PropertyLoader.getDefaultProperties;

import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import uk.co.strangeskies.modabi.ModabiException;
import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.io.ModabiIOException;
import uk.co.strangeskies.modabi.io.structured.NavigableStructuredDataReader;
import uk.co.strangeskies.modabi.io.structured.StructuredDataPosition;
import uk.co.strangeskies.modabi.io.structured.StructuredDataReader;

public class XmlSource implements StructuredDataReader {
  private final XMLStreamReader in;
  private final StructuredDataPositionImpl currentLocation;

  private QualifiedName currentChild;
  private QualifiedName nextChild;
  private final List<String> comments;
  private final Map<QualifiedName, String> properties;
  private String primaryProperty;

  private final NamespaceStack namespaceStack;

  private XmlSource(XMLStreamReader in) {
    this.in = in;
    currentLocation = new StructuredDataPositionImpl();

    comments = new ArrayList<>();
    properties = new HashMap<>();

    namespaceStack = new NamespaceStack();

    pumpEvents();
  }

  public static StructuredDataReader from(ReadableByteChannel in) {
    return from(createXMLStreamReader(in));
  }

  public static StructuredDataReader from(XMLStreamReader in) {
    return new XmlSource(in);
  }

  private static XMLStreamReader createXMLStreamReader(ReadableByteChannel in) {
    try {
      return XMLInputFactory.newInstance().createXMLStreamReader(newInputStream(in));
    } catch (XMLStreamException | FactoryConfigurationError e) {
      throw new ModabiException("", e);
    }
  }

  @Override
  public Namespace getDefaultNamespaceHint() {
    return namespaceStack.getDefaultNamespace();
  }

  @Override
  public Stream<Namespace> getNamespaceHints() {
    return namespaceStack.getAliasSet().getNamespaces();
  }

  @Override
  public StructuredDataReader readNextChild() {
    if (nextChild == null)
      return null;

    currentLocation.push();

    return pumpEvents();
  }

  @Override
  public Optional<QualifiedName> getNextChild() {
    return Optional.ofNullable(nextChild);
  }

  private StructuredDataReader pumpEvents() {
    if (nextChild != null)
      fillProperties();

    currentChild = nextChild;
    nextChild = null;

    comments.clear();

    boolean done = false;
    do {
      int code;
      try {
        code = in.next();
      } catch (XMLStreamException e) {
        throw new ModabiIOException(
            getDefaultProperties(ModabiXmlExceptionMessages.class).problemReadingFromXmlDocument(),
            e);
      }

      switch (code) {
      case XMLStreamReader.START_ELEMENT:
        primaryProperty = "";

        QName name = in.getName();
        QualifiedName qualifiedName = new QualifiedName(
            name.getLocalPart(),
            Namespace.parseHttpString(name.getNamespaceURI()));

        nextChild = qualifiedName;

        done = true;
        break;
      case XMLStreamReader.END_DOCUMENT:
      case XMLStreamReader.END_ELEMENT:
        done = true;
        break;
      case XMLStreamReader.COMMENT:
        comments.add(in.getText());
        break;
      case XMLStreamReader.CHARACTERS:
        primaryProperty += in.getText();
        break;
      }
    } while (!done);

    return this;
  }

  private void fillProperties() {
    /*
     * Namespaces:
     */
    namespaceStack.push();
    for (int i = 0; i < in.getNamespaceCount(); i++) {
      namespaceStack
          .addNamespace(Namespace.parseHttpString(in.getNamespaceURI(i)), in.getNamespacePrefix(i));
    }
    String defaultNamespaceString = in.getNamespaceURI();
    if (defaultNamespaceString != null)
      namespaceStack.setDefaultNamespace(Namespace.parseHttpString(defaultNamespaceString));

    /*
     * Properties:
     */
    properties.clear();

    for (int i = 0; i < in.getAttributeCount(); i++) {
      String namespaceString = in.getAttributeNamespace(i);
      if (namespaceString == null)
        namespaceString = in.getNamespaceContext().getNamespaceURI("");
      Namespace namespace = Namespace.parseHttpString(namespaceString);

      QualifiedName propertyName = new QualifiedName(in.getAttributeLocalName(i), namespace);

      properties.put(propertyName, in.getAttributeValue(i));
    }
  }

  @Override
  public Stream<QualifiedName> getProperties() {
    return properties.keySet().stream();
  }

  @Override
  public Optional<String> readProperty(QualifiedName name) {
    return Optional.ofNullable(properties.get(name));
  }

  @Override
  public StructuredDataReader endChild() {
    if (nextChild != null)
      while (pumpEvents() != null)
        ;
    currentLocation.pop();

    pumpEvents();

    namespaceStack.pop();

    return this;
  }

  @Override
  public Stream<String> getComments() {
    return comments.stream();
  }

  @Override
  public StructuredDataReader split() {
    throw new AssertionError();
  }

  @Override
  public NavigableStructuredDataReader buffer() {
    throw new AssertionError();
  }

  @Override
  public QualifiedName getName() {
    return currentChild;
  }

  @Override
  public Optional<String> readPrimaryProperty() {
    return Optional.ofNullable(primaryProperty);
  }

  @Override
  public StructuredDataPosition getPosition() {
    return currentLocation;
  }
}
