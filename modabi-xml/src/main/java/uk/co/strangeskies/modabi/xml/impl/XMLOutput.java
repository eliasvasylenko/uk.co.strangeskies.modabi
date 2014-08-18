package uk.co.strangeskies.modabi.xml.impl;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import uk.co.strangeskies.modabi.data.io.DataItem;
import uk.co.strangeskies.modabi.data.io.TerminatingDataTarget;
import uk.co.strangeskies.modabi.data.io.structured.BufferingStructuredDataTarget;
import uk.co.strangeskies.modabi.data.io.structured.StructuredDataTarget;
import uk.co.strangeskies.modabi.namespace.Namespace;
import uk.co.strangeskies.modabi.namespace.QualifiedName;

public class XMLOutput implements StructuredDataTarget {
	private final BufferingStructuredDataTarget bufferingOutput;
	private int depth;

	public XMLOutput() {
		bufferingOutput = new BufferingStructuredDataTarget();
		depth = 0;
	}

	class PipeTarget implements StructuredDataTarget {
		private Namespace namespace;
		private boolean openingElement;
		private boolean hasChildren;
		private final Deque<QualifiedName> elementStack;
		private final Map<Namespace, String> aliases;

		private String indent = "";

		public PipeTarget() {
			openingElement = false;
			hasChildren = false;
			elementStack = new ArrayDeque<>();
			aliases = new HashMap<>();
		}

		@Override
		public StructuredDataTarget namespace(Namespace namespace) {
			this.namespace = namespace;
			aliases.put(namespace, "");

			return this;
		}

		private String nameString(QualifiedName name) {
			String alias = aliases.get(name.getNamespace());
			if (alias != null)
				return (alias.equals("") ? "" : alias + ":") + name.getName();
			else
				return name.toString();
		}

		@Override
		public StructuredDataTarget nextChild(QualifiedName name) {
			endProperties();

			String line = indent + "<" + nameString(name);

			if (elementStack.isEmpty() && namespace != null)
				line += " xmlns=\"" + namespace + "\"";

			System.out.print(line);
			indent += "  ";

			openingElement = true;
			hasChildren = false;

			elementStack.push(name);

			return this;
		}

		@Override
		public TerminatingDataTarget property(QualifiedName name) {
			System.out.print(" " + nameString(name) + "=");
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

				System.out
						.println(indent + "</" + nameString(elementStack.pop()) + ">");
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
	public StructuredDataTarget namespace(Namespace namespace) {
		bufferingOutput.namespace(namespace);

		return this;
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
		if (depth-- == 1)
			bufferingOutput.buffer().pipeNextChild(new PipeTarget());

		return this;
	}
}
