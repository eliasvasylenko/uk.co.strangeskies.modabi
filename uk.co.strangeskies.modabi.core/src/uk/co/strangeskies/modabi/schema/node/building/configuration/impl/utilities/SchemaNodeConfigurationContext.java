package uk.co.strangeskies.modabi.schema.node.building.configuration.impl.utilities;

import java.util.List;

import uk.co.strangeskies.modabi.namespace.Namespace;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.node.ChildNode;
import uk.co.strangeskies.modabi.schema.node.SchemaNode;
import uk.co.strangeskies.modabi.schema.node.building.DataLoader;

public interface SchemaNodeConfigurationContext<T extends ChildNode<?, ?>> {
	DataLoader dataLoader();

	boolean isAbstract();

	boolean isInputExpected();

	boolean isInputDataOnly();

	boolean isConstructorExpected();

	boolean isStaticMethodExpected();

	Namespace namespace();

	Class<?> inputTargetClass(QualifiedName node);

	Class<?> outputSourceClass();

	void addChild(T result);

	<U extends T> List<U> overrideChild(QualifiedName id, Class<U> nodeClass);

	List<? extends SchemaNode<?, ?>> overriddenNodes();
}
