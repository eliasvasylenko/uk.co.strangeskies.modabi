package uk.co.strangeskies.modabi.xml.impl;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.co.strangeskies.modabi.data.io.DataItem;
import uk.co.strangeskies.modabi.data.io.TerminatingDataTarget;
import uk.co.strangeskies.modabi.data.io.structured.BufferingStructuredDataTarget;
import uk.co.strangeskies.modabi.data.io.structured.StructuredDataTarget;
import uk.co.strangeskies.modabi.namespace.Namespace;
import uk.co.strangeskies.modabi.namespace.QualifiedName;

public class XMLOutput implements StructuredDataTarget {
	private Namespace defaultNamespace;
	private final Map<Namespace, String> namespaceAliases;

	private final BufferingStructuredDataTarget bufferingOutput;
	private int depth;

	public XMLOutput() {
		bufferingOutput = new BufferingStructuredDataTarget();
		depth = 0;
		namespaceAliases = new HashMap<>();
	}

	class PipeTarget implements StructuredDataTarget {
		private boolean openingElement;
		private boolean hasChildren;
		private final Deque<QualifiedName> elementStack;

		private String indent = "";

		public PipeTarget() {
			openingElement = false;
			hasChildren = false;
			elementStack = new ArrayDeque<>();
		}

		@Override
		public StructuredDataTarget defaultNamespaceHint(Namespace namespace) {
			return this;
		}

		@Override
		public StructuredDataTarget nextChild(QualifiedName name) {
			endProperties();

			String line = indent + "<" + getNameString(name);

			if (elementStack.isEmpty()) {
				if (defaultNamespace != null)
					line += " xmlns=\"" + defaultNamespace.toHttpString() + "\"";

				for (Map.Entry<Namespace, String> alias : namespaceAliases.entrySet())
					line += " xmlns:" + alias.getValue() + "=\""
							+ alias.getKey().toHttpString() + "\"";
			}

			System.out.print(line);
			indent += "  ";

			openingElement = true;
			hasChildren = false;

			elementStack.push(name);

			return this;
		}

		@Override
		public TerminatingDataTarget property(QualifiedName name) {
			System.out.print(" " + getNameString(name) + "=");
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

				System.out.println(indent + "</" + getNameString(elementStack.pop())
						+ ">");
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
	public StructuredDataTarget defaultNamespaceHint(Namespace namespace) {
		this.defaultNamespace = namespace;

		return this;
	}

	@Override
	public String composeQualifiedName(QualifiedName name) {
		String nameString = tryGetNameString(name);

		if (nameString == null) {
			List<String> namespace = Arrays.asList(name.getNamespace().getPackage()
					.getName().split("\\."));
			String alias = namespace.get(namespace.size() - 1);
			namespaceAliases.put(name.getNamespace(), alias);
			nameString = alias + ":" + name.getName();
		}

		return nameString;
	}

	private String tryGetNameString(QualifiedName name) {
		if (name.getNamespace().equals(defaultNamespace))
			return name.getName();

		String alias = namespaceAliases.get(name.getNamespace());
		if (alias != null)
			return alias + ":" + name.getName();
		else
			return null;
	}

	private String getNameString(QualifiedName name) {
		String nameString = tryGetNameString(name);

		if (nameString == null)
			throw new AssertionError();

		return nameString;
	}

	@Override
	public StructuredDataTarget nextChild(QualifiedName name) {
		composeQualifiedName(name);
		bufferingOutput.nextChild(name);
		depth++;

		return this;
	}

	@Override
	public TerminatingDataTarget property(QualifiedName name) {
		composeQualifiedName(name);
		return bufferingOutput.property(name);
	}

	@Override
	public TerminatingDataTarget content() {
		return bufferingOutput.content();
	}

	@Override
	public StructuredDataTarget endChild() {
		bufferingOutput.endChild();
		if (depth-- == 1)
			bufferingOutput.buffer().pipeNextChild(new PipeTarget());

		return this;
	}
}
