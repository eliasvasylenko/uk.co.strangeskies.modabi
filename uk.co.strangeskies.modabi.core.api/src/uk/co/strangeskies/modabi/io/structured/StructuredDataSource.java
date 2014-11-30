package uk.co.strangeskies.modabi.io.structured;

import java.util.Set;

import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.namespace.Namespace;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.SchemaException;

public interface StructuredDataSource {
	public StructuredDataState currentState();

	public Namespace getDefaultNamespaceHint();

	public Set<Namespace> getNamespaceHints();

	public Set<String> getComments();

	public QualifiedName startNextChild();

	public QualifiedName peekNextChild();

	public default void startNextChild(QualifiedName name) {
		QualifiedName nextName = startNextChild();
		if (!nextName.equals(name)) {
			throw new SchemaException("Next child '" + nextName
					+ "' does not match expected name '" + name + "'.");
		}
	}

	public Set<QualifiedName> getProperties();

	public DataSource readProperty(QualifiedName name);

	public DataSource readContent();

	public boolean hasNextChild();

	public default boolean skipNextChild() {
		boolean hasNext = hasNextChild();
		if (hasNext) {
			startNextChild();
			skipChildren();
			endChild();
		}
		return hasNext;
	}

	public default void skipChildren() {
		while (skipNextChild())
			;
	}

	/**
	 * throws an exception if there are more children, so call skipChildren()
	 * first if you want to ignore them.
	 */
	public void endChild();

	public int depth();

	public int indexAtDepth();

	public default <T extends StructuredDataTarget> T pipeNextChild(T output) {
		if (getDefaultNamespaceHint() != null)
			output.registerDefaultNamespaceHint(getDefaultNamespaceHint());

		for (Namespace hint : getNamespaceHints())
			output.registerNamespaceHint(hint);

		QualifiedName childElement;

		int depth = 0;
		do {
			while ((childElement = startNextChild()) != null) {
				output.nextChild(childElement);

				if (getDefaultNamespaceHint() != null)
					output.registerDefaultNamespaceHint(getDefaultNamespaceHint());
				for (Namespace hint : getNamespaceHints())
					output.registerNamespaceHint(hint);

				for (QualifiedName property : getProperties())
					readProperty(property).pipe(output.writeProperty(property))
							.terminate();

				DataSource content = readContent();
				if (content != null)
					content.pipe(output.writeContent()).terminate();

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
