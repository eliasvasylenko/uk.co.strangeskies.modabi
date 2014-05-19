package uk.co.strangeskies.modabi.data.io.structured;

import java.util.Set;

import uk.co.strangeskies.modabi.data.io.TerminatingDataSource;

public interface StructuredDataSource {
	public String nextChild();

	public Set<String> properties();

	public TerminatingDataSource propertyData(String name);

	public TerminatingDataSource content();

	public void endChild();

	public int depth();

	public int indexAtDepth();

	public default <T extends StructuredDataTarget> T pipeNextChild(T output) {
		String childElement;

		int depth = 0;
		do {
			while ((childElement = nextChild()) != null) {
				output.nextChild(childElement);

				for (String property : properties())
					propertyData(property).pipe(output.property(property)).terminate();

				TerminatingDataSource content = content();
				if (content != null)
					content.pipe(output.content()).terminate();

				depth++;
			}
			output.endChild();
			endChild();

			depth--;
		} while (depth > 0);

		return output;
	}

	public default BufferedStructuredDataSource bufferNextChild() {
		return pipeNextChild(new BufferingStructuredDataTarget()).buffer();
	}
}
