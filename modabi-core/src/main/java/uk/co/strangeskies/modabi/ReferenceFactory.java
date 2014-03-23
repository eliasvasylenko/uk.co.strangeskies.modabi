package uk.co.strangeskies.modabi;

import uk.co.strangeskies.modabi.model.Model;
import uk.co.strangeskies.modabi.namespace.QualifiedName;

public interface ReferenceFactory {
	public ReferenceFactory model(Model<?> model);

	public ReferenceFactory reference(QualifiedName name);
}
