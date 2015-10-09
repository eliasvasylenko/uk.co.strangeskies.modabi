/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.xml.provider.
 *
 * uk.co.strangeskies.modabi.xml.provider is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.xml.provider is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.xml.provider.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi.io.xml;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import javanet.staxutils.IndentingXMLStreamWriter;
import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.NamespaceAliases;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.io.DataTarget;
import uk.co.strangeskies.modabi.io.IOException;
import uk.co.strangeskies.modabi.io.structured.StructuredDataTargetImpl;

public class XmlTarget extends StructuredDataTargetImpl<XmlTarget> {
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
			throw new IOException(e);
		}
	}

	public XmlTarget(OutputStream out, boolean formatted) {
		this(createXMLStreamWriter(out, formatted));
	}

	private static XMLStreamWriter createXMLStreamWriter(OutputStream out,
			boolean formatted) {
		try {
			XMLOutputFactory factory = XMLOutputFactory.newInstance();
			XMLStreamWriter writer = factory.createXMLStreamWriter(out);
			if (formatted)
				writer = new IndentingXMLStreamWriter(writer);
			return writer;
		} catch (XMLStreamException | FactoryConfigurationError e) {
			throw new IOException(e);
		}
	}

	public XmlTarget(OutputStream out) {
		this(out, true);
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
		boolean done = currentChild != null;
		try {
			if (done) {
				// write start of element
				if (selfClosing) {
					out.writeEmptyElement(currentChild.getNamespace().toHttpString(),
							currentChild.getName());
				} else {
					out.writeStartElement(currentChild.getNamespace().toHttpString(),
							currentChild.getName());
				}

				// write namespaces
				if (namespaces.getDefaultNamespace() != null)
					out.writeDefaultNamespace(
							namespaces.getDefaultNamespace().toHttpString());
				for (Namespace namespace : namespaces.getNamespaces())
					out.writeNamespace(namespaces.getNamespaceAlias(namespace),
							namespace.toHttpString());
				namespaces.push();

				// write properties
				for (QualifiedName property : properties.keySet())
					out.writeAttribute(property.getNamespace().toHttpString(),
							property.getName(), properties.get(property));
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
		} catch (XMLStreamException e) {
			throw new IOException(e);
		}
		return done;
	}

	@Override
	public void nextChildImpl(QualifiedName name) {
		outputCurrentChild(false);
		currentChild = name;
	}

	@Override
	public void endChildImpl() {
		try {
			if (!outputCurrentChild(true))
				out.writeEndElement();
		} catch (XMLStreamException e) {
			throw new IOException(e);
		}

		namespaces.pop();
		if (namespaces.isBase())
			try {
				out.writeEndDocument();
				out.flush();
			} catch (XMLStreamException e) {
				throw new IOException(e);
			}
	}

	@Override
	public DataTarget writePropertyImpl(QualifiedName name) {
		return DataTarget.composeString(s -> properties.put(name, s),
				this::composeName);
	}

	@Override
	public DataTarget writeContentImpl() {
		outputCurrentChild(false);

		return DataTarget.composeString(s -> {
			try {
				out.writeCharacters(s);
			} catch (XMLStreamException e) {
				throw new IOException(e);
			}
		} , this::composeName);
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
			throw new IOException(e);
		}
	}
}

class NamespaceStack implements NamespaceContext {
	private Namespace defaultNamespace;
	private NamespaceAliases aliasSet;

	private NamespaceStack next;

	public NamespaceStack() {
		aliasSet = new NamespaceAliases();
	}

	private NamespaceStack(NamespaceStack from) {
		setFrom(from);
	}

	private void setFrom(NamespaceStack from) {
		defaultNamespace = from.defaultNamespace;
		aliasSet = from.aliasSet;
		next = from.next;
	}

	public String getNameString(QualifiedName name) {
		if (defaultNamespace != null
				&& defaultNamespace.equals(name.getNamespace()))
			return name.getName();

		String alias = aliasSet.getAlias(name.getNamespace());
		if (alias != null)
			return alias + ":" + name.getName();

		if (!isBase())
			return next.getNameString(name);

		return name.toString();
	}

	public void push() {
		next = new NamespaceStack(this);
		aliasSet = new NamespaceAliases();
		defaultNamespace = null;
	}

	public void pop() {
		if (isBase())
			throw new AssertionError();

		setFrom(next);
	}

	public boolean isBase() {
		return next == null;
	}

	public void setDefaultNamespace(Namespace namespace) {
		defaultNamespace = namespace;
	}

	public Namespace getDefaultNamespace() {
		return defaultNamespace;
	}

	public String addNamespace(Namespace namespace) {
		String alias = getNamespaceAlias(namespace);
		if (alias != null)
			return alias;

		return aliasSet.addNamespace(namespace);
	}

	public Set<Namespace> getNamespaces() {
		return aliasSet.getNamespaces();
	}

	public String getNamespaceAlias(Namespace namespace) {
		if (namespace.equals(defaultNamespace))
			return XMLConstants.DEFAULT_NS_PREFIX;

		if (getNamespaces().contains(namespace))
			return aliasSet.getAlias(namespace);

		if (next != null)
			return next.getNamespaceAlias(namespace);

		return null;
	}

	@Override
	public String getNamespaceURI(String prefix) {
		return aliasSet.getNamespace(prefix).toHttpString();
	}

	@Override
	public String getPrefix(String namespaceURI) {
		return getNamespaceAlias(Namespace.parseHttpString(namespaceURI));
	}

	@Override
	public Iterator<String> getPrefixes(String namespaceURI) {
		return Arrays.asList(getPrefix(namespaceURI)).iterator();
	}
}
