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

import uk.co.strangeskies.modabi.data.io.DataSource;
import uk.co.strangeskies.modabi.data.io.IOException;
import uk.co.strangeskies.modabi.data.io.structured.StructuredDataSource;
import uk.co.strangeskies.modabi.data.io.structured.StructuredDataSourceDecorator;
import uk.co.strangeskies.modabi.data.io.structured.StructuredDataState;
import uk.co.strangeskies.modabi.namespace.Namespace;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.SchemaException;

public class XMLSource extends StructuredDataSourceDecorator {
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

				System.out.println("{}{} " + code + " " + in.getName());

				switch (code) {
				case XMLStreamReader.START_ELEMENT:
					QName name = in.getName();
					nextChild = new QualifiedName(name.getLocalPart(),
							Namespace.parseHttpString(name.getNamespaceURI()));

					properties.clear();

					System.out.println(" ???? " + name + " " + in.getAttributeCount());
					for (int i = 0; i < in.getAttributeCount(); i++) {
						QualifiedName propertyName = new QualifiedName(
								in.getAttributeLocalName(i), Namespace.parseHttpString(in
										.getAttributeNamespace(i)));

						properties.put(propertyName,
								DataSource.parseString(in.getAttributeValue(i)));

						System.out.println(" ? " + i + " " + propertyName + " / "
								+ in.getAttributeValue(i));
					}
					break;
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
		public Set<QualifiedName> getProperties() {
			return new HashSet<>(properties.keySet());
		}

		@Override
		public DataSource readProperty(QualifiedName name) {
			return properties.get(name);
		}

		@Override
		public DataSource readContent() {
			return DataSource.parseString(content);
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
		public Set<String> getComments() {
			return comments;
		}

		@Override
		public StructuredDataState currentState() {
			return null;
		}
	}
}
