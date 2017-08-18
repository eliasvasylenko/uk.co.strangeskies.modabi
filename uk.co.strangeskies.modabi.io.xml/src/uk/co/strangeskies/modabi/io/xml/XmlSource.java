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

import static uk.co.strangeskies.text.properties.PropertyLoader.getDefaultProperties;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import uk.co.strangeskies.modabi.ModabiException;
import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.io.ModabiIOException;
import uk.co.strangeskies.modabi.io.structured.BufferableStructuredDataSourceImpl;
import uk.co.strangeskies.modabi.io.structured.NavigableStructuredDataReader;
import uk.co.strangeskies.modabi.io.structured.StructuredDataReader;
import uk.co.strangeskies.modabi.io.structured.StructuredDataReaderWrapper;
import uk.co.strangeskies.modabi.io.structured.StructuredDataState;

public class XmlSource implements StructuredDataReader {
	private final XMLStreamReader in;
	private final List<Integer> currentLocation;

	private QualifiedName nextChild;
	private final List<String> comments;
	private final Map<QualifiedName, DataSource> properties;
	private String content;

	private final NamespaceStack namespaceStack;

	private XmlSource(XMLStreamReader in) {
		this.in = in;
		currentLocation = new ArrayList<>();

		comments = new ArrayList<>();
		properties = new HashMap<>();

		namespaceStack = new NamespaceStack();

		pumpEvents();
	}

	public static StructuredDataReaderWrapper from(InputStream in) {
		return from(createXMLStreamReader(in));
	}

	public static StructuredDataReaderWrapper from(XMLStreamReader in) {
		return new BufferableStructuredDataSourceImpl(new XmlSource(in));
	}

	private static XMLStreamReader createXMLStreamReader(InputStream in) {
		try {
			return XMLInputFactory.newInstance().createXMLStreamReader(in);
		} catch (XMLStreamException | FactoryConfigurationError e) {
			throw new ModabiException("", e);
		}
	}

	@Override
	public Namespace getDefaultNamespaceHint() {
		return namespaceStack.getDefaultNamespace();
	}

	@Override
	public Set<Namespace> getNamespaceHints() {
		return namespaceStack.getAliasSet().getNamespaces();
	}

	@Override
	public QualifiedName readNextChild() {
		if (nextChild == null)
			return null;

		currentLocation.add(0);

		return pumpEvents();
	}

	@Override
	public QualifiedName peekNextChild() {
		return nextChild;
	}

	@Override
	public boolean hasNextChild() {
		return nextChild != null;
	}

	private QualifiedName pumpEvents() {
		if (nextChild != null)
			fillProperties();

		QualifiedName thisChild = nextChild;
		nextChild = null;

		comments.clear();

		boolean done = false;
		do {
			int code;
			try {
				code = in.next();
			} catch (XMLStreamException e) {
				throw new ModabiIOException(getDefaultProperties(ModabiXmlExceptionMessages.class).problemReadingFromXmlDocument(), e);
			}

			switch (code) {
			case XMLStreamReader.START_ELEMENT:
				content = "";

				QName name = in.getName();
				QualifiedName qualifiedName = new QualifiedName(name.getLocalPart(),
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
				content += in.getText();
				break;
			}
		} while (!done);

		return thisChild;
	}

	private void fillProperties() {
		/*
		 * Namespaces:
		 */
		namespaceStack.push();
		for (int i = 0; i < in.getNamespaceCount(); i++) {
			namespaceStack.addNamespace(Namespace.parseHttpString(in.getNamespaceURI(i)), in.getNamespacePrefix(i));
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

			properties.put(propertyName, DataSource.parseString(in.getAttributeValue(i), parseName()));
		}
	}

	private Function<String, QualifiedName> parseName() {
		NamespaceStack namespaceStack = this.namespaceStack.copy();

		return name -> {
			String[] splitName = name.split(":", 2);

			String prefix;
			if (splitName.length == 2)
				prefix = splitName[0];
			else
				prefix = "";

			Namespace namespace = namespaceStack.getNamespace(prefix);

			if (namespace == null)
				throw new ModabiException(
						"Cannot find namespace with prefix '" + prefix + "' for name '" + name + "' in current context");

			return new QualifiedName(splitName[splitName.length - 1], namespace);
		};
	}

	@Override
	public Set<QualifiedName> getProperties() {
		return new HashSet<>(properties.keySet());
	}

	@Override
	public DataSource readProperty(QualifiedName name) {
		return properties.get(name);
	}

	@Override
	public DataSource readContent() {
		if (content == null || content.trim().equals("")) {
			return null;
		} else {
			return DataSource.parseString(content, parseName());
		}
	}

	@Override
	public StructuredDataReader endChild() {
		if (nextChild != null)
			while (pumpEvents() != null)
				;
		currentLocation.remove(currentLocation.size() - 1);
		if (!currentLocation.isEmpty()) {
			currentLocation.add(currentLocation.remove(currentLocation.size() - 1) + 1);
		}

		pumpEvents();

		namespaceStack.pop();

		return this;
	}

	@Override
	public List<Integer> index() {
		return Collections.unmodifiableList(currentLocation);
	}

	@Override
	public List<String> getComments() {
		return comments;
	}

	@Override
	public StructuredDataState currentState() {
		throw new AssertionError();
	}

	@Override
	public StructuredDataReader split() {
		throw new AssertionError();
	}

	@Override
	public NavigableStructuredDataReader buffer() {
		throw new AssertionError();
	}
}
