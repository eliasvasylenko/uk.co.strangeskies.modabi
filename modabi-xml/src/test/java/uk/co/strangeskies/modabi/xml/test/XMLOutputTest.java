package uk.co.strangeskies.modabi.xml.test;

import uk.co.strangeskies.modabi.data.io.DataType;
import uk.co.strangeskies.modabi.data.io.structured.StructuredDataTarget;
import uk.co.strangeskies.modabi.xml.impl.XMLOutput;

public class XMLOutputTest {
	private void run() {
		StructuredDataTarget output = new XMLOutput();

		output.nextChild("root");
		output.nextChild("poot");
		output.content().put(DataType.DOUBLE, 2d).put(DataType.STRING, "coot")
				.terminate();
		output.property("groot").put(DataType.BOOLEAN, true).terminate();
		output.endChild();
		output.nextChild("joot");
		output.endChild();
		output.endChild();
	}

	public static void main(String... args) {
		new XMLOutputTest().run();
	}
}
