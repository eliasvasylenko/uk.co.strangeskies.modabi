package uk.co.strangeskies.modabi.xml.impl;

import java.util.ArrayDeque;
import java.util.Deque;

import uk.co.strangeskies.modabi.data.io.DataItem;
import uk.co.strangeskies.modabi.data.io.TerminatingDataTarget;
import uk.co.strangeskies.modabi.data.io.structured.BufferingStructuredDataTarget;
import uk.co.strangeskies.modabi.data.io.structured.StructuredDataTarget;

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
		private final Deque<String> elementStack;

		private String indent = "";

		public PipeTarget() {
			openingElement = false;
			hasChildren = false;
			elementStack = new ArrayDeque<>();
		}

		@Override
		public StructuredDataTarget nextChild(String name) {
			endProperties();

			elementStack.push(name);

			System.out.print(indent + "<" + name);
			indent += "  ";

			openingElement = true;

			hasChildren = false;

			return this;
		}

		@Override
		public TerminatingDataTarget property(String name) {
			System.out.print(" " + name + "=");
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
	public StructuredDataTarget nextChild(String name) {
		bufferingOutput.nextChild(name);
		depth++;

		return this;
	}

	@Override
	public TerminatingDataTarget property(String name) {
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
