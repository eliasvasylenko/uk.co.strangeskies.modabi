package uk.co.strangeskies.modabi;

import uk.co.strangeskies.modabi.namespace.QualifiedName;

public interface ModelLoader {
	public void modelName(QualifiedName name);

	public void data(QualifiedName name);
}
