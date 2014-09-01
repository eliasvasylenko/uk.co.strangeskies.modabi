package uk.co.strangeskies.modabi.xml.impl;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javanet.staxutils.IndentingXMLStreamWriter;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import uk.co.strangeskies.modabi.data.io.IOException;
import uk.co.strangeskies.modabi.data.io.TerminatingDataTarget;
import uk.co.strangeskies.modabi.data.io.structured.StructuredDataTarget;
import uk.co.strangeskies.modabi.data.io.structured.StructuredDataTargetDecorator;
import uk.co.strangeskies.modabi.namespace.Namespace;
import uk.co.strangeskies.modabi.namespace.QualifiedName;

class NamespaceStack {
	private Namespace defaultNamespace;
	private Map<Namespace, String> namespaceAliases;

	private NamespaceStack next;

	public NamespaceStack() {
		namespaceAliases = new HashMap<>();
	}

	private NamespaceStack(NamespaceStack from) {
		setFrom(from);
	}

	private void setFrom(NamespaceStack from) {
		defaultNamespace = from.defaultNamespace;
		namespaceAliases = from.namespaceAliases;
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

	private String generateAlias() {
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
		if (defaultNamespace != null)
			throw new AssertionError();
		defaultNamespace = namespace;
	}

	public Namespace getDefaultNamespace() {
		return defaultNamespace;
	}

	public String addNamespace(Namespace namespace) {
		return "test";
	}

	public Set<Namespace> getNamespaces() {
		return namespaceAliases.keySet();
	}

	public String getNamespaceAlias(Namespace namespace) {
		return "test";
	}
}

class XMLTargetImpl implements StructuredDataTarget {
	private final XMLStreamWriter out;

	private final NamespaceStack namespaces;

	private QualifiedName currentChild;
	private List<String> comments;

	public XMLTargetImpl(XMLStreamWriter out) {
		this.out = out;
		namespaces = new NamespaceStack();

		try {
			out.writeStartDocument();
		} catch (XMLStreamException | FactoryConfigurationError e) {
			throw new IOException(e);
		}
	}

	@Override
	public StructuredDataTarget registerDefaultNamespaceHint(Namespace namespace) {
		try {
			namespaces.setDefaultNamespace(namespace);
			out.setDefaultNamespace(namespace.toHttpString());
		} catch (XMLStreamException e) {
			throw new IOException(e);
		}

		return this;
	}

	@Override
	public StructuredDataTarget registerNamespaceHint(Namespace namespace) {
		try {
			if (!namespaceAliases.peek().containsKey(namespace))
				namespaceAliases.peek().put(namespace, alias);
			if (namespaceAliases.size() > 1)
				out.writeNamespace(alias, namespace.toHttpString());
		} catch (XMLStreamException e) {
			throw new IOException(e);
		}

		return this;
	}

	@Override
	public StructuredDataTarget nextChild(QualifiedName name) {
		outputCurrentChild();

		currentChild = name;

		try {
			if (namespaces.isBase()) {
				out.writeStartElement(
						namespaces.getNamespaceAlias(name.getNamespace()), name.getName(),
						name.getNamespace().toHttpString());

				for (Namespace namespace : namespaceAliases.peek().keySet())
					if (namespace != name.getNamespace())
						out.writeNamespace(namespaceAliases.peek().get(namespace),
								namespace.toHttpString());
			} else
				out.writeStartElement(name.getNamespace().toHttpString(),
						name.getName());
		} catch (XMLStreamException e) {
			throw new IOException(e);
		}

		namespaces.push();

		return this;
	}

	private void outputCurrentChild() {
		if (currentChild != null) {
			try {
				out.writeStartElement(currentChild.getNamespace().toHttpString(),
						currentChild.getName());

				out.writeDefaultNamespace(namespaces.getDefaultNamespace()
						.toHttpString());
				for (Namespace namespace : namespaces.getNamespaces())
					out.writeNamespace(namespaces.getNamespaceAlias(namespace),
							namespace.toHttpString());
			} catch (XMLStreamException e) {
				throw new IOException(e);
			}

			currentChild = null;
		}
	}

	@Override
	public StructuredDataTarget endChild() {
		outputCurrentChild();

		try {
			out.writeEndElement();
		} catch (XMLStreamException e) {
			throw new IOException(e);
		}

		namespaces.pop();
		if (namespaces.isBase())
			try {
				out.writeEndDocument();
			} catch (XMLStreamException e) {
				throw new IOException(e);
			}

		return this;
	}

	@Override
	public TerminatingDataTarget property(QualifiedName name) {
		return TerminatingDataTarget.composeString(
				s -> {
					try {
						out.writeAttribute(name.getNamespace().toHttpString(),
								name.getName(), s);
					} catch (XMLStreamException e) {
						throw new IOException(e);
					}
				}, this::formatName);
	}

	@Override
	public TerminatingDataTarget content() {
		outputCurrentChild();

		return TerminatingDataTarget.composeString(s -> {
			try {
				out.writeCharacters(s);
			} catch (XMLStreamException e) {
				throw new IOException(e);
			}
		}, this::formatName);
	}

	private String formatName(QualifiedName name) {
		String prefix = out.getNamespaceContext().getPrefix(
				name.getNamespace().toHttpString());
		if (prefix.length() > 0)
			prefix += ":";
		return prefix + name;
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
	public State currentState() {
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
