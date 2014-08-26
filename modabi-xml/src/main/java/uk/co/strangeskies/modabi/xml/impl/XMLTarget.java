package uk.co.strangeskies.modabi.xml.impl;

import java.io.OutputStream;
import java.util.function.Consumer;

import javanet.staxutils.IndentingXMLStreamWriter;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import uk.co.strangeskies.modabi.data.io.DataItem;
import uk.co.strangeskies.modabi.data.io.DataType;
import uk.co.strangeskies.modabi.data.io.IOException;
import uk.co.strangeskies.modabi.data.io.TerminatingDataTarget;
import uk.co.strangeskies.modabi.data.io.structured.StructuredDataTarget;
import uk.co.strangeskies.modabi.namespace.Namespace;
import uk.co.strangeskies.modabi.namespace.QualifiedName;

public class XMLTarget implements StructuredDataTarget {
	private final XMLStreamWriter out;
	private int depth = 0;

	public XMLTarget(XMLStreamWriter out) {
		this.out = out;
		try {
			out.writeStartDocument();
		} catch (XMLStreamException | FactoryConfigurationError e) {
			throw new IOException(e);
		}
	}

	public XMLTarget(OutputStream out, boolean formatted) {
		this(createXMLStreamWriter(out, formatted));
	}

	public XMLTarget(OutputStream out) {
		this(out, true);
	}

	private static XMLStreamWriter createXMLStreamWriter(OutputStream out,
			boolean formatted) {
		try {
			XMLStreamWriter writer = XMLOutputFactory.newInstance()
					.createXMLStreamWriter(out);
			if (formatted)
				writer = new IndentingXMLStreamWriter(writer);
			return writer;
		} catch (XMLStreamException | FactoryConfigurationError e) {
			throw new IOException(e);
		}
	}

	private String generateAlias(Namespace namespace) {
		return "test";
	}

	@Override
	public StructuredDataTarget registerDefaultNamespaceHint(Namespace namespace) {
		try {
			out.writeDefaultNamespace(namespace.toHttpString());
		} catch (XMLStreamException e) {
			throw new IOException(e);
		}

		return this;
	}

	@Override
	public StructuredDataTarget registerNamespaceHint(Namespace namespace) {
		String alias = generateAlias(namespace);

		try {
			out.writeNamespace(alias, namespace.toHttpString());
		} catch (XMLStreamException e) {
			throw new IOException(e);
		}

		return this;
	}

	@Override
	public StructuredDataTarget nextChild(QualifiedName name) {
		try {
			out.writeStartElement(name.getNamespace().toHttpString(), name.getName());
		} catch (XMLStreamException e) {
			throw new IOException(e);
		}

		depth++;

		return this;
	}

	@Override
	public StructuredDataTarget endChild() {
		try {
			out.writeEndElement();
		} catch (XMLStreamException e) {
			throw new IOException(e);
		}

		if (--depth == 0)
			try {
				out.writeEndDocument();
			} catch (XMLStreamException e) {
				throw new IOException(e);
			}

		return this;
	}

	@Override
	public TerminatingDataTarget property(QualifiedName name) {
		return getDataSink(s -> {
			try {
				out.writeAttribute(name.getNamespace().toHttpString(), name.getName(),
						s);
			} catch (XMLStreamException e) {
				throw new IOException(e);
			}
		});
	}

	@Override
	public TerminatingDataTarget content() {
		return getDataSink(s -> {
			try {
				out.writeCharacters(s);
			} catch (XMLStreamException e) {
				throw new IOException(e);
			}
		});
	}

	private TerminatingDataTarget getDataSink(Consumer<String> resultConsumer) {
		return new TerminatingDataTarget() {
			private boolean terminated;
			private boolean compound;

			StringBuilder stringBuilder = new StringBuilder();

			private void next(Object value) {
				if (compound)
					stringBuilder.append(", ");
				else
					compound = true;
				stringBuilder.append(value);
			}

			@Override
			public <T> TerminatingDataTarget put(DataItem<T> item) {
				if (terminated)
					throw new IOException();

				if (item.type() == DataType.QUALIFIED_NAME) {
					QualifiedName name = (QualifiedName) item.data();

					String prefix = out.getNamespaceContext().getPrefix(
							name.getNamespace().toHttpString());
					if (prefix.length() > 0)
						prefix += ":";

					next(prefix + name.getName());
				} else
					next(item.data());
				return this;
			}

			@Override
			public void terminate() {
				resultConsumer.accept(stringBuilder.toString());

				terminated = true;
			}

			@Override
			public boolean isTerminated() {
				return terminated;
			}
		};
	}
}
