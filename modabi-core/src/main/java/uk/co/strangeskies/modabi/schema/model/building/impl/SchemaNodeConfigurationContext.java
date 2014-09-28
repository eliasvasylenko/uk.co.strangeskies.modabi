package uk.co.strangeskies.modabi.schema.model.building.impl;

import java.util.List;

import uk.co.strangeskies.modabi.namespace.Namespace;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.model.building.DataLoader;
import uk.co.strangeskies.modabi.schema.model.nodes.ChildNode;

public interface SchemaNodeConfigurationContext<T extends ChildNode<?, ?>> {
	DataLoader getDataLoader();

	boolean isAbstract();

	boolean isDataContext();

	boolean hasInput();

	Class<?> getInputTargetClass(QualifiedName node);

	Class<?> getOutputSourceClass();

	void addChild(T result);

	<U extends T> List<U> overrideChild(QualifiedName id, Class<U> nodeClass);

	Namespace getNamespace();
}
