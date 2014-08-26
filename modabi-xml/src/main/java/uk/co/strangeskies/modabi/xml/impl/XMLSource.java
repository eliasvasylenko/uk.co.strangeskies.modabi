package uk.co.strangeskies.modabi.xml.impl;

import java.io.InputStream;
import java.util.Set;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import uk.co.strangeskies.modabi.data.io.TerminatingDataSource;
import uk.co.strangeskies.modabi.data.io.structured.StructuredDataSource;
import uk.co.strangeskies.modabi.namespace.Namespace;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.SchemaException;

public class XMLSource implements StructuredDataSource {
	private final XMLStreamReader in;

	public XMLSource(XMLStreamReader in) {
		this.in = in;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Namespace> namespaceHints() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public QualifiedName nextChild() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<QualifiedName> properties() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TerminatingDataSource propertyData(QualifiedName name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TerminatingDataSource content() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void endChild() {
		// TODO Auto-generated method stub

	}

	@Override
	public int depth() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int indexAtDepth() {
		// TODO Auto-generated method stub
		return 0;
	}
}
