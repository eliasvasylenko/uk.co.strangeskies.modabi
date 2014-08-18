package uk.co.strangeskies.modabi.model.building.impl;

import java.util.LinkedHashSet;

import uk.co.strangeskies.modabi.model.building.DataLoader;
import uk.co.strangeskies.modabi.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.namespace.Namespace;
import uk.co.strangeskies.modabi.namespace.QualifiedName;

public interface SchemaNodeConfigurationContext<T extends ChildNode<?, ?>> {
	DataLoader getDataLoader();

	boolean isAbstract();

	Class<?> getInputTargetClass();

	Class<?> getOutputTargetClass();

	void addChild(T result);

	<U extends T> LinkedHashSet<U> overrideChild(QualifiedName id,
			Class<U> nodeClass);

	Namespace getNamespace();
}
