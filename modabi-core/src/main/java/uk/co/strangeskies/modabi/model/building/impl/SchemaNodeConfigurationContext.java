package uk.co.strangeskies.modabi.model.building.impl;

import java.util.LinkedHashSet;

import uk.co.strangeskies.modabi.model.building.DataLoader;
import uk.co.strangeskies.modabi.model.nodes.ChildNode;

public interface SchemaNodeConfigurationContext<T extends ChildNode<?>> {
	DataLoader getDataLoader();

	boolean isAbstract();

	Class<?> getCurrentChildInputTargetClass();

	Class<?> getCurrentChildOutputTargetClass();

	void addChild(T result);

	<U extends T> LinkedHashSet<U> overrideChild(String id, Class<U> nodeClass);
}
