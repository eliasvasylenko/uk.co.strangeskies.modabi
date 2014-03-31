package uk.co.strangeskies.modabi.xml.impl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.Deque;

import uk.co.strangeskies.modabi.data.StructuredDataOutput;
import uk.co.strangeskies.modabi.data.TerminatingDataSink;

public class XMLOutput implements StructuredDataOutput {
	private String indent = "";

	boolean openingElement = false;
	boolean hasChildren = false;
	Deque<String> elementStack = new ArrayDeque<>();

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
	public TerminatingDataSink property(String name) {
		System.out.print(" " + name + "=");
		return getDataSink(true);
	}

	@Override
	public TerminatingDataSink content() {
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

	private TerminatingDataSink getDataSink(boolean property) {
		if (property)
			System.out.print("\"");

		return new TerminatingDataSink() {
			boolean compound = false;

			private void next(Object value) {
				if (compound)
					System.out.print(", ");
				else
					compound = true;
				System.out.print(value);
			}

			@Override
			public TerminatingDataSink string(String value) {
				next(value);
				return this;
			}

			@Override
			public TerminatingDataSink longValue(long value) {
				next(value);
				return this;
			}

			@Override
			public TerminatingDataSink integer(BigInteger value) {
				next(value);
				return this;
			}

			@Override
			public TerminatingDataSink intValue(int value) {
				next(value);
				return this;
			}

			@Override
			public TerminatingDataSink floatValue(float value) {
				next(value);
				return this;
			}

			@Override
			public TerminatingDataSink doubleValue(double value) {
				next(value);
				return this;
			}

			@Override
			public TerminatingDataSink decimal(BigDecimal value) {
				next(value);
				return this;
			}

			@Override
			public TerminatingDataSink booleanValue(boolean value) {
				next(value);
				return this;
			}

			@Override
			public TerminatingDataSink binary(byte[] value) {
				next(value);
				return this;
			}

			@Override
			public void end() {
				if (property)
					System.out.print('"');
				else
					System.out.println();
			}
		};
	}
}
