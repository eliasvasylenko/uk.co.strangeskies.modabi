package uk.co.strangeskies.modabi.xml.impl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.Deque;

import uk.co.strangeskies.modabi.data.io.TerminatingDataTarget;
import uk.co.strangeskies.modabi.data.io.structured.BufferedStructuredInput;
import uk.co.strangeskies.modabi.data.io.structured.BufferingStructuredOutput;
import uk.co.strangeskies.modabi.data.io.structured.StructuredOutput;

public class XMLOutput implements StructuredOutput {
	private final BufferingStructuredOutput bufferingOutput;
	private int depth;

	public XMLOutput() {
		bufferingOutput = BufferedStructuredInput.from();
		depth = 0;
	}

	class PipeTarget implements StructuredOutput {
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
		public void childElement(String name) {
			endProperties();

			elementStack.push(name);

			System.out.print(indent + "<" + name);
			indent += "  ";

			openingElement = true;

			hasChildren = false;
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
		public void endElement() {
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
				public TerminatingDataTarget string(String value) {
					next(value);
					return this;
				}

				@Override
				public TerminatingDataTarget longValue(long value) {
					next(value);
					return this;
				}

				@Override
				public TerminatingDataTarget integer(BigInteger value) {
					next(value);
					return this;
				}

				@Override
				public TerminatingDataTarget intValue(int value) {
					next(value);
					return this;
				}

				@Override
				public TerminatingDataTarget floatValue(float value) {
					next(value);
					return this;
				}

				@Override
				public TerminatingDataTarget doubleValue(double value) {
					next(value);
					return this;
				}

				@Override
				public TerminatingDataTarget decimal(BigDecimal value) {
					next(value);
					return this;
				}

				@Override
				public TerminatingDataTarget booleanValue(boolean value) {
					next(value);
					return this;
				}

				@Override
				public TerminatingDataTarget binary(byte[] value) {
					next(value);
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
	public void childElement(String name) {
		bufferingOutput.childElement(name);
		depth++;
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
	public void endElement() {
		bufferingOutput.endElement();
		if (depth-- == 1)
			bufferingOutput.buffer().pipeNextChild(new PipeTarget());
	}
}
