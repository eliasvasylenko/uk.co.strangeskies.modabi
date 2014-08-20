package uk.co.strangeskies.modabi.xml.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import uk.co.strangeskies.modabi.data.io.DataItem;
import uk.co.strangeskies.modabi.data.io.TerminatingDataTarget;
import uk.co.strangeskies.modabi.data.io.structured.BufferingStructuredDataTarget;
import uk.co.strangeskies.modabi.data.io.structured.StructuredDataTarget;
import uk.co.strangeskies.modabi.namespace.Namespace;
import uk.co.strangeskies.modabi.namespace.QualifiedName;

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

public class XMLOutput implements StructuredDataTarget {
	private final BufferingStructuredDataTarget bufferingOutput;
	private int depth;

	public XMLOutput() {
		bufferingOutput = new BufferingStructuredDataTarget();
		depth = 0;
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
			System.out.print(" xmlns=\"" + namespace.toHttpString() + "\"");
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
			System.out.print(" xmlns:" + alias + "=\"" + namespace.toHttpString()
					+ "\"");
		}

		@Override
		public StructuredDataTarget nextChild(QualifiedName name) {
			endProperties();

			System.out.print(indent + "<" + elementStack.getNameString(name));

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
			System.out.print(" " + elementStack.getNameString(name) + "=");
			return getDataSink(true);
		}

		@Override
		public TerminatingDataTarget content() {
			endProperties();
			hasChildren = true;

			System.out.print(indent);
			return getDataSink(false);
		}

		private void endProperties() {
			if (openingElement)
				System.out.println(">");
			openingElement = false;
		}

		@Override
		public StructuredDataTarget endChild() {
			indent = indent.substring(2);
			if (!hasChildren) {
				System.out.println(" />");

				openingElement = false;
				hasChildren = true;
				elementStack.pop();
			} else {
				endProperties();

				System.out.println(indent + "</" + elementStack.pop() + ">");
			}

			return this;
		}

		private TerminatingDataTarget getDataSink(boolean property) {
			if (property)
				System.out.print("\"");

			return new TerminatingDataTarget() {
				boolean compound = false;
				private boolean terminated;

				private void next(Object value) {
					if (compound)
						System.out.print(", ");
					else
						compound = true;
					System.out.print(value);
				}

				@Override
				public <T> TerminatingDataTarget put(DataItem<T> item) {
					next(item.data());
					return this;
				}

				@Override
				public void terminate() {
					if (property)
						System.out.print('"');
					else
						System.out.println();

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
