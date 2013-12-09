package uk.co.strangeskies.modabi;

import java.util.Set;

import uk.co.strangeskies.modabi.data.DataType;
import uk.co.strangeskies.modabi.node.BindingNode;
import uk.co.strangeskies.modabi.processing.QualifiedName;

public interface Schema<T> {
	public QualifiedName getQualifiedName();

	public Set<Schema<?>> getDependencies();

	public Set<DataType<?>> getTypeSet();

	public Set<BindingNode<?>> getModelSet();

	public BindingNode<T> getRoot();
}
