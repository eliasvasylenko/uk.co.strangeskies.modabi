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
package uk.co.strangeskies.modabi.xml.impl;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javanet.staxutils.IndentingXMLStreamWriter;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import uk.co.strangeskies.modabi.io.DataTarget;
import uk.co.strangeskies.modabi.io.IOException;
import uk.co.strangeskies.modabi.io.structured.StructuredDataState;
import uk.co.strangeskies.modabi.io.structured.StructuredDataTarget;
import uk.co.strangeskies.modabi.io.structured.StructuredDataTargetDecorator;
import uk.co.strangeskies.modabi.namespace.Namespace;
import uk.co.strangeskies.modabi.namespace.QualifiedName;

class NamespaceStack implements NamespaceContext {
	private Namespace defaultNamespace;
	private Map<Namespace, String> namespaceAliases;
	private Map<String, Namespace> aliasedNamespaces;

	private NamespaceStack next;

	public NamespaceStack() {
		namespaceAliases = new HashMap<>();
		aliasedNamespaces = new HashMap<>();
	}

	private NamespaceStack(NamespaceStack from) {
		setFrom(from);
	}

	private void setFrom(NamespaceStack from) {
		defaultNamespace = from.defaultNamespace;
		namespaceAliases = from.namespaceAliases;
		aliasedNamespaces = from.aliasedNamespaces;
		next = from.next;
	}

	public String getNameString(QualifiedName name) {
		if (defaultNamespace != null
				&& defaultNamespace.equals(name.getNamespace()))
			return name.getName();

		String alias = namespaceAliases.get(name.getNamespace());
		if (alias != null)
			return alias + ":" + name.getName();

		if (!isBase())
			return next.getNameString(name);

		return name.toString();
	}

	public void push() {
		next = new NamespaceStack(this);
		namespaceAliases = new HashMap<>();
		aliasedNamespaces = new HashMap<>();
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

	private String generateAlias(Namespace namespace) {
		Set<String> existingAliases = new HashSet<>(namespaceAliases.values());
		NamespaceStack next = this.next;
		while (next != null) {
			existingAliases.addAll(next.namespaceAliases.values());
			next = next.next;
		}

		String alias = "";
		do {
			alias += "a";
		} while (existingAliases.contains(alias));

		return alias;
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

		alias = generateAlias(namespace);
		namespaceAliases.put(namespace, alias);
		aliasedNamespaces.put(alias, namespace);

		return alias;
	}

	public Set<Namespace> getNamespaces() {
		return namespaceAliases.keySet();
	}

	public String getNamespaceAlias(Namespace namespace) {
		if (namespace.equals(defaultNamespace))
			return XMLConstants.DEFAULT_NS_PREFIX;

		if (namespaceAliases.containsKey(namespace))
			return namespaceAliases.get(namespace);

		if (next != null)
			return next.getNamespaceAlias(namespace);

		return null;
	}

	@Override
	public String getNamespaceURI(String prefix) {
		return aliasedNamespaces.get(prefix).toHttpString();
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

class XMLTargetImpl implements StructuredDataTarget {
	private final XMLStreamWriter out;

	private final NamespaceStack namespaces;
	private boolean started;

	private QualifiedName currentChild;
	private final Map<QualifiedName, String> properties;
	private final List<String> comments;

	public XMLTargetImpl(XMLStreamWriter out) {
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

	@Override
	public StructuredDataTarget registerDefaultNamespaceHint(Namespace namespace) {
		namespaces.setDefaultNamespace(namespace);
		return this;
	}

	@Override
	public StructuredDataTarget registerNamespaceHint(Namespace namespace) {
		namespaces.addNamespace(namespace);
		return this;
	}

	private boolean outputCurrentChild(boolean selfClosing) {
		boolean done = currentChild != null;
		try {
			if (done) {
				// write start of element
				if (selfClosing)
					out.writeEmptyElement(currentChild.getNamespace().toHttpString(),
							currentChild.getName());
				else
					out.writeStartElement(currentChild.getNamespace().toHttpString(),
							currentChild.getName());

				// write namespaces
				if (namespaces.getDefaultNamespace() != null)
					out.writeDefaultNamespace(namespaces.getDefaultNamespace()
							.toHttpString());
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
	public StructuredDataTarget nextChild(QualifiedName name) {
		outputCurrentChild(false);

		currentChild = name;

		return this;
	}

	@Override
	public StructuredDataTarget endChild() {
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

		return this;
	}

	@Override
	public DataTarget writeProperty(QualifiedName name) {
		return DataTarget.composeString(s -> properties.put(name, s),
				this::composeName);
	}

	@Override
	public DataTarget writeContent() {
		outputCurrentChild(false);

		return DataTarget.composeString(s -> {
			try {
				out.writeCharacters(s);
			} catch (XMLStreamException e) {
				throw new IOException(e);
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
	public StructuredDataTarget comment(String comment) {
		try {
			out.writeComment(comment);
		} catch (XMLStreamException e) {
			throw new IOException(e);
		}
		return this;
	}

	@Override
	public StructuredDataState currentState() {
		return null;
	}
}

public class XMLTarget extends StructuredDataTargetDecorator {
	public XMLTarget(XMLStreamWriter out) {
		super(new XMLTargetImpl(out));
	}

	public XMLTarget(OutputStream out, boolean formatted) {
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

	public XMLTarget(OutputStream out) {
		this(out, true);
	}
}
