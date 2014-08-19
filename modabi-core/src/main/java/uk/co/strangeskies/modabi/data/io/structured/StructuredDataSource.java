package uk.co.strangeskies.modabi.data.io.structured;

import java.util.Set;

import uk.co.strangeskies.modabi.data.io.TerminatingDataSource;
import uk.co.strangeskies.modabi.namespace.Namespace;
import uk.co.strangeskies.modabi.namespace.QualifiedName;

public interface StructuredDataSource {
	public Namespace globalDefaultNamespaceHint();

	public Set<Namespace> globalNamespaceHints();

	public Namespace defaultNamespaceHint();

	public Set<Namespace> namespaceHints();

	public QualifiedName nextChild();

	public Set<QualifiedName> properties();

	public TerminatingDataSource propertyData(QualifiedName name);

	public TerminatingDataSource content();

	public void endChild();

	public int depth();

	public int indexAtDepth();

	public default <T extends StructuredDataTarget> T pipeNextChild(T output) {
		if (globalDefaultNamespaceHint() != null)
			output.registerDefaultNamespaceHint(globalDefaultNamespaceHint(), true);

		if (!globalNamespaceHints().isEmpty())
			for (Namespace hint : globalNamespaceHints())
				output.registerNamespaceHint(hint, true);

		QualifiedName childElement;

		int depth = 0;
		do {
			while ((childElement = nextChild()) != null) {
				output.nextChild(childElement);

				if (defaultNamespaceHint() != null)
					output.registerDefaultNamespaceHint(defaultNamespaceHint(), false);
				for (Namespace hint : namespaceHints())
					output.registerNamespaceHint(hint, false);

				for (QualifiedName property : properties())
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
