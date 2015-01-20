package uk.co.strangeskies.modabi.schema.node.building.configuration.impl.utilities;

import java.util.List;

import uk.co.strangeskies.modabi.namespace.Namespace;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.node.ChildNode;
import uk.co.strangeskies.modabi.schema.node.SchemaNode;
import uk.co.strangeskies.modabi.schema.node.building.DataLoader;
import uk.co.strangeskies.reflection.TypeLiteral;

public interface SchemaNodeConfigurationContext<T extends ChildNode<?, ?>> {
	DataLoader dataLoader();

	boolean isAbstract();

	boolean isInputExpected();

	boolean isInputDataOnly();

	boolean isConstructorExpected();

	boolean isStaticMethodExpected();

	Namespace namespace();

	TypeLiteral<?> inputTargetType(QualifiedName node);

	TypeLiteral<?> outputSourceType();

	void addChild(T result);

	<U extends T> List<U> overrideChild(QualifiedName id, TypeLiteral<U> nodeClass);

	List<? extends SchemaNode<?, ?>> overriddenNodes();
}
