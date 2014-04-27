package uk.co.strangeskies.modabi.data.io.structured;

import java.util.Set;

import uk.co.strangeskies.modabi.data.io.TerminatingDataSource;

public interface StructuredInput {
	public String nextChild();

	public Set<String> properties();

	public TerminatingDataSource propertyData(String name);

	public TerminatingDataSource content();

	public void endElement();

	public default <T extends StructuredOutput> T pipeNextChild(T output) {
		String childElement;

		int depth = 0;
		do {
			while ((childElement = nextChild()) != null) {
				output.childElement(childElement);

				for (String property : properties())
					propertyData(property).pipe(output.property(property)).terminate();

				TerminatingDataSource content = content();
				if (content != null)
					content.pipe(output.content()).terminate();

				depth++;
			}
			output.endElement();
			endElement();

			depth--;
		} while (depth > 0);

		return output;
	}

	public default BufferedStructuredInput bufferNextChild() {
		return pipeNextChild(BufferedStructuredInput.from()).buffer();
	}
}
