package uk.co.strangeskies.modabi.model.impl;

import java.util.Set;

import uk.co.strangeskies.modabi.model.nodes.ChildNode;

public interface SchemaNodeConfigurationContext<T extends ChildNode> {
	Class<?> getCurrentChildInputTargetClass();

	Class<?> getCurrentChildOutputTargetClass();

	void addChild(T result, T effective);

	<U extends T> Set<U> overrideChild(String id, Class<U> nodeClass);
}
