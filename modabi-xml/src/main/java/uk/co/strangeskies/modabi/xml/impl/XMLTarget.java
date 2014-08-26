package uk.co.strangeskies.modabi.xml.impl;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import uk.co.strangeskies.modabi.data.io.DataItem;
import uk.co.strangeskies.modabi.data.io.DataType;
import uk.co.strangeskies.modabi.data.io.TerminatingDataTarget;
import uk.co.strangeskies.modabi.data.io.structured.StructuredDataTarget;
import uk.co.strangeskies.modabi.namespace.Namespace;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.SchemaException;

public class XMLTarget implements StructuredDataTarget {
	private final XMLStreamWriter out;

	public XMLTarget(OutputStream out) {
		try {
			this.out = XMLOutputFactory.newInstance().createXMLStreamWriter(out);
		} catch (XMLStreamException | FactoryConfigurationError e) {
			throw new SchemaException(e);
		}
	}

	class PipeTarget implements StructuredDataTarget {
		private boolean openingElement;
		private boolean hasChildren;

		private final StackElement elementStack;

		private String indent = "";

		public PipeTarget() {
			openingElement = false;
			hasChildren = false;
			elementStack = new StackElement();
		}

		@Override
		public StructuredDataTarget registerDefaultNamespaceHint(
				Namespace namespace, boolean global) {
			elementStack.setDefaultNamespace(namespace);

			if (!global)
				printDefaultNamespace(namespace);

			return this;
		}

		private void printDefaultNamespace(Namespace namespace) {
			out.print(" xmlns=\"" + namespace.toHttpString() + "\"");
		}

		@Override
		public StructuredDataTarget registerNamespaceHint(Namespace namespace,
				boolean global) {
			String alias = elementStack.addNamespace(namespace);

			if (!global)
				printNamespace(namespace, alias);

			return this;
		}

		private void printNamespace(Namespace namespace, String alias) {
			out.print(" xmlns:" + alias + "=\"" + namespace.toHttpString() + "\"");
		}

		@Override
		public StructuredDataTarget nextChild(QualifiedName name) {
			endProperties();

			out.print(indent + "<" + elementStack.getNameString(name));

			if (elementStack.isBase()) {
				if (elementStack.getDefaultNamespace() != null)
					printDefaultNamespace(elementStack.getDefaultNamespace());

				for (Namespace namespace : elementStack.getNamespaces())
					printNamespace(namespace, elementStack.getNamespaceAlias(namespace));
			}

			indent += "  ";

			openingElement = true;
			hasChildren = false;

			elementStack.push(name);

			return this;
		}

		@Override
		public TerminatingDataTarget property(QualifiedName name) {
			out.print(" " + elementStack.getNameString(name) + "=");
			return getDataSink(true);
		}

		@Override
		public TerminatingDataTarget content() {
			endProperties();
			hasChildren = true;

			out.print(indent);
			return getDataSink(false);
		}

		private void endProperties() {
			if (openingElement)
				out.println(">");
			openingElement = false;
		}

		@Override
		public StructuredDataTarget endChild() {
			indent = indent.substring(2);
			if (!hasChildren) {
				out.println(" />");

				openingElement = false;
				hasChildren = true;
				elementStack.pop();
			} else {
				endProperties();

				out.println(indent + "</" + elementStack.pop() + ">");
			}

			return this;
		}

		private TerminatingDataTarget getDataSink(boolean property) {
			if (property)
				out.print("\"");

			return new TerminatingDataTarget() {
				boolean compound = false;
				private boolean terminated;

				private void next(Object value) {
					if (compound)
						out.print(", ");
					else
						compound = true;
					out.print(value);
				}

				@Override
				public <T> TerminatingDataTarget put(DataItem<T> item) {
					if (item.type() == DataType.QUALIFIED_NAME)
						next(elementStack.getNameString((QualifiedName) item.data()));
					else
						next(item.data());
					return this;
				}

				@Override
				public void terminate() {
					if (property)
						out.print('"');
					else
						out.println();

					terminated = true;
				}

				@Override
				public boolean isTerminated() {
					return terminated;
				}
			};
		}
	}

	@Override
	public StructuredDataTarget nextChild(QualifiedName name) {
		bufferingOutput.nextChild(name);
		depth++;

		return this;
	}

	@Override
	public TerminatingDataTarget property(QualifiedName name) {
		return bufferingOutput.property(name);
	}

	@Override
	public TerminatingDataTarget content() {
		return bufferingOutput.content();
	}

	@Override
	public StructuredDataTarget endChild() {
		bufferingOutput.endChild();
		if (--depth == 0)
			bufferingOutput.buffer().pipeNextChild(new PipeTarget());

		return this;
	}

	@Override
	public StructuredDataTarget registerDefaultNamespaceHint(Namespace namespace,
			boolean global) {
		return bufferingOutput.registerDefaultNamespaceHint(namespace, global);
	}

	@Override
	public StructuredDataTarget registerNamespaceHint(Namespace namespace,
			boolean global) {
		return bufferingOutput.registerNamespaceHint(namespace, global);
	}
}

class StackElement {
	private Namespace defaultNamespace;
	private Map<Namespace, String> namespaceAliases;
	private QualifiedName qualifiedName;

	private StackElement next;

	public StackElement() {
		this.qualifiedName = null;
		namespaceAliases = new HashMap<>();
	}

	private StackElement(StackElement from) {
		setFrom(from);
	}

	private void setFrom(StackElement from) {
		defaultNamespace = from.defaultNamespace;
		namespaceAliases = from.namespaceAliases;
		qualifiedName = from.qualifiedName;
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

	public void push(QualifiedName name) {
		next = new StackElement(this);
		qualifiedName = name;
		namespaceAliases = new HashMap<>();
		defaultNamespace = null;
	}

	public String pop() {
		if (isBase())
			throw new AssertionError();

		String name = getNameString(qualifiedName);

		setFrom(next);

		return name;
	}

	public boolean isBase() {
		return qualifiedName == null;
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