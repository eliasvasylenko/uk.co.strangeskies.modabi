package uk.co.strangeskies.modabi.xml.impl;

import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.io.IOException;
import uk.co.strangeskies.modabi.io.structured.StructuredDataSource;
import uk.co.strangeskies.modabi.io.structured.StructuredDataSourceDecorator;
import uk.co.strangeskies.modabi.io.structured.StructuredDataState;
import uk.co.strangeskies.modabi.namespace.Namespace;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.SchemaException;

public class XMLSource extends StructuredDataSourceDecorator implements
		StructuredDataSource {
	public XMLSource(XMLStreamReader in) {
		super(new XMLSourceImpl(in));
	}

	public XMLSource(InputStream in) {
		super(new XMLSourceImpl(in));
	}

	private static class XMLSourceImpl implements StructuredDataSource {
		private final XMLStreamReader in;
		private final Deque<Integer> currentLocation;

		private QualifiedName nextChild;
		private final Set<String> comments;
		private final Map<QualifiedName, DataSource> properties;
		private String content;

		public XMLSourceImpl(XMLStreamReader in) {
			this.in = in;
			currentLocation = new ArrayDeque<>();

			comments = new HashSet<>();
			properties = new HashMap<>();

			pumpEvents();
		}

		public XMLSourceImpl(InputStream in) {
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
		public Namespace getDefaultNamespaceHint() {
			return Namespace.parseHttpString(in.getNamespaceURI());
		}

		@Override
		public Set<Namespace> getNamespaceHints() {
			Set<Namespace> namespaces = new HashSet<>();
			for (int i = 0; i < in.getNamespaceCount(); i++)
				namespaces.add(Namespace.parseHttpString(in.getNamespaceURI(i)));
			return namespaces;
		}

		@Override
		public QualifiedName startNextChild() {
			if (nextChild == null)
				throw new IOException();

			currentLocation.push(0);

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
					content = in.getText();
					break;
				}
			} while (!done);

			return thisChild;
		}

		private void fillProperties() {
			properties.clear();

			for (int i = 0; i < in.getAttributeCount(); i++) {
				String namespaceString = in.getAttributeNamespace(i);
				if (namespaceString == null)
					namespaceString = in.getNamespaceContext().getNamespaceURI("");
				Namespace namespace = Namespace.parseHttpString(namespaceString);

				QualifiedName propertyName = new QualifiedName(
						in.getAttributeLocalName(i), namespace);

				properties.put(propertyName,
						DataSource.parseString(in.getAttributeValue(i), this::parseName));
			}
		}

		private QualifiedName parseName(String name) {
			String[] splitName = name.split(":", 2);

			String prefix;
			if (splitName.length == 2)
				prefix = splitName[0];
			else
				prefix = "";

			return new QualifiedName(splitName[splitName.length - 1],
					Namespace.parseHttpString(in.getNamespaceContext().getNamespaceURI(
							prefix)));
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
			return DataSource.parseString(content, this::parseName);
		}

		@Override
		public void endChild() {
			if (nextChild != null)
				while (pumpEvents() != null)
					;
			currentLocation.pop();
			currentLocation.push(currentLocation.pop() + 1);

			pumpEvents();
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
		public Set<String> getComments() {
			return comments;
		}

		@Override
		public StructuredDataState currentState() {
			return null;
		}
	}
}
