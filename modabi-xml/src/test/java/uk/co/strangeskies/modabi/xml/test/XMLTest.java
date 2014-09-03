package uk.co.strangeskies.modabi.xml.test;

import uk.co.strangeskies.modabi.data.io.DataType;
import uk.co.strangeskies.modabi.data.io.structured.StructuredDataTarget;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.xml.impl.XMLTarget;

public class XMLTest {
	private void run() {
		StructuredDataTarget output = new XMLTarget(System.out);

		output.nextChild(new QualifiedName("root"));
		output.nextChild(new QualifiedName("poot"));
		output.writeContent().put(DataType.DOUBLE, 2d).put(DataType.STRING, "coot")
				.terminate();
		output.writeProperty(new QualifiedName("groot")).put(DataType.BOOLEAN, true)
				.terminate();
		output.endChild();
		output.nextChild(new QualifiedName("joot"));
		output.endChild();
		output.endChild();
	}

	public static void main(String... args) {
		new XMLTest().run();
	}
}
