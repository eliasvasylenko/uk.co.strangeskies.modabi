package uk.co.strangeskies.modabi.model.impl;

import java.util.List;

import uk.co.strangeskies.modabi.model.nodes.ChildNode;

public interface SchemaNodeConfigurationContext<T extends ChildNode> {
	Class<?> getCurrentChildInputTargetClass();

	Class<?> getCurrentChildOutputTargetClass();

	void addChild(T result, T effective);

	<U extends T> List<U> overrideChild(String id, Class<U> nodeClass);
}
