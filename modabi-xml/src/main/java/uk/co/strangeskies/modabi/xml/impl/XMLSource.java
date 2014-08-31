package uk.co.strangeskies.modabi.xml.impl;

import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import uk.co.strangeskies.modabi.data.io.IOException;
import uk.co.strangeskies.modabi.data.io.TerminatingDataSource;
import uk.co.strangeskies.modabi.data.io.structured.StructuredDataSource;
import uk.co.strangeskies.modabi.namespace.Namespace;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.SchemaException;

public class XMLSource implements StructuredDataSource {
	private final XMLStreamReader in;
	private final Deque<Integer> currentLocation;

	private QualifiedName nextChild;
	private final Set<String> comments;
	private String content;

	public XMLSource(XMLStreamReader in) {
		this.in = in;
		currentLocation = new ArrayDeque<>();

		comments = new HashSet<>();
	}

	public XMLSource(InputStream in) {
		this(createXMLStreamReader(in));
	}

	private static XMLStreamReader createXMLStreamReader(InputStream in) {
		try {
			return XMLInputFactory.newInstance().createXMLStreamReader(in);
		} catch (XMLStreamException | FactoryConfigurationError e) {
			throw new SchemaException(e);
		}
	}

	@Override
	public Namespace defaultNamespaceHint() {
		return Namespace.parseHttpString(in.getNamespaceURI());
	}

	@Override
	public Set<Namespace> namespaceHints() {
		Set<Namespace> namespaces = new HashSet<>();
		for (int i = 0; i < in.getNamespaceCount(); i++)
			namespaces.add(Namespace.parseHttpString(in.getNamespaceURI(i)));
		return namespaces;
	}

	@Override
	public QualifiedName nextChild() {
		if (nextChild == null)
			throw new IOException();

		currentLocation.push(0);

		return pumpEvents();
	}

	private QualifiedName pumpEvents() {
		QualifiedName thisChild = nextChild;
		nextChild = null;
		comments.clear();
		content = null;

		boolean done = false;
		do {
			int code;
			try {
				code = in.next();
			} catch (XMLStreamException e) {
				throw new IOException(e);
			}

			switch (code) {
			case XMLStreamReader.START_ELEMENT:
				QName name = in.getName();
				nextChild = new QualifiedName(name.getLocalPart(),
						Namespace.parseHttpString(name.getNamespaceURI()));
			case XMLStreamReader.END_ELEMENT:
			case XMLStreamReader.END_DOCUMENT:
				done = true;
				break;
			case XMLStreamReader.COMMENT:
				comments.add(in.getText());
				break;
			case XMLStreamReader.CHARACTERS:
				content = in.getText();
				break;
			}
		} while (!done);

		return thisChild;
	}

	@Override
	public Set<QualifiedName> properties() {
		Set<QualifiedName> properties = new HashSet<>();
		for (int i = 0; i < in.getNamespaceCount(); i++)
			properties.add(new QualifiedName(in.getAttributeLocalName(i), Namespace
					.parseHttpString(in.getAttributeNamespace(i))));
		return properties;
	}

	@Override
	public TerminatingDataSource propertyData(QualifiedName name) {
		return TerminatingDataSource.parseString(in.getAttributeValue(name
				.getNamespace().toHttpString(), name.getName()));
	}

	@Override
	public TerminatingDataSource content() {
		return TerminatingDataSource.parseString(content);
	}

	@Override
	public void endChild() {
		while (pumpEvents() != null)
			;
		currentLocation.pop();
		currentLocation.push(currentLocation.pop() + 1);
	}

	@Override
	public int depth() {
		return currentLocation.size();
	}

	@Override
	public int indexAtDepth() {
		return currentLocation.peek();
	}

	@Override
	public Set<String> comments() {
		return comments;
	}
}
