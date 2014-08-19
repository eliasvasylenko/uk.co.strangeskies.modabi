package uk.co.strangeskies.modabi.xml.impl;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import uk.co.strangeskies.modabi.data.io.DataItem;
import uk.co.strangeskies.modabi.data.io.TerminatingDataTarget;
import uk.co.strangeskies.modabi.data.io.structured.BufferingStructuredDataTarget;
import uk.co.strangeskies.modabi.data.io.structured.StructuredDataTarget;
import uk.co.strangeskies.modabi.namespace.Namespace;
import uk.co.strangeskies.modabi.namespace.QualifiedName;

public class XMLOutput implements StructuredDataTarget {
	public class StackElement {
		private Namespace defaultNamespace;
		private final Map<Namespace, String> namespaceAliases;
		private final QualifiedName qualifiedName;

		public StackElement(QualifiedName qualifiedName) {
			this.qualifiedName = qualifiedName;
			namespaceAliases = new HashMap<>();
		}

		public StackElement(Namespace defaultNamespace, Set<Namespace> namespaces) {
			this.qualifiedName = null;
			this.defaultNamespace = defaultNamespace;
			namespaceAliases = new HashMap<>();
			
			for (namespaces);
		}
	}

	private final BufferingStructuredDataTarget bufferingOutput;
	private int depth;

	public XMLOutput() {
		bufferingOutput = new BufferingStructuredDataTarget();
		depth = 0;
	}

	class PipeTarget implements StructuredDataTarget {
		private boolean openingElement;
		private boolean hasChildren;

		private Namespace defaultNamespace;
		private final Set<Namespace> namespaces;

		private final Deque<StackElement> elementStack;

		private String indent = "";

		public PipeTarget() {
			openingElement = false;
			hasChildren = false;
			elementStack = new ArrayDeque<>();
			namespaces = new HashSet<>();
		}

		@Override
		public StructuredDataTarget registerDefaultNamespaceHint(
				Namespace namespace, boolean global) {
			if (global)
				defaultNamespace = namespace;
			else {
				System.out.print(" xmlns=\"" + namespace.toHttpString() + "\"");
			}

			return this;
		}

		@Override
		public StructuredDataTarget registerNamespaceHint(Namespace namespace,
				boolean global) {
			if (global)
				namespaces.add(namespace);
			else {
				String alias = "test";
				System.out.print(" xmlns:" + alias + "=\"" + namespace.toHttpString()
						+ "\"");
			}

			return this;
		}

		@Override
		public StructuredDataTarget nextChild(QualifiedName name) {
			endProperties();

			System.out.print(indent + "<" + getNameString(name));

			if (elementStack.isEmpty()) {
				if (defaultNamespace != null)
					registerDefaultNamespaceHint(defaultNamespace, false);

				for (Namespace namespace : namespaces)
					registerNamespaceHint(namespace, false);
			}

			indent += "  ";

			openingElement = true;
			hasChildren = false;

			elementStack.push(new StackElement(name));

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

				System.out.println(indent + "</"
						+ getNameString(elementStack.pop().qualifiedName) + ">");
			}

			return this;
		}

		private String getNameString(QualifiedName name) {
			Spliterator<StackElement> spliterator = Spliterators
					.spliteratorUnknownSize(elementStack.descendingIterator(),
							Spliterator.ORDERED);

			return Stream
					.concat(StreamSupport.stream(spliterator, false),
							Stream.of(new StackElement(defaultNamespace, namespaces)))
					.map(
							e -> {
								if (e.defaultNamespace != null
										&& e.defaultNamespace.equals(name.getNamespace()))
									return name.getName();
								else if (e.namespaces.contains(name.getNamespace()))
									return null;
								else
									return null;
							}).filter(Objects::nonNull).findFirst()
					.orElseThrow(IllegalArgumentException::new);
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
