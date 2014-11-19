package uk.co.strangeskies.modabi.schema.management.binding;

import uk.co.strangeskies.modabi.io.structured.StructuredDataSource;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.management.Provisions;
import uk.co.strangeskies.modabi.schema.node.DataNode;
import uk.co.strangeskies.modabi.schema.node.model.Model;
import uk.co.strangeskies.modabi.schema.node.type.DataBindingType;
import uk.co.strangeskies.utilities.collection.computingmap.ComputingMap;

public interface BindingContext extends BindingState {
	Provisions provisions();

	Model.Effective<?> getModel(QualifiedName nextElement);

	<T> ComputingMap<DataBindingType.Effective<? extends T>, DataNode.Effective<? extends T>> getDataNodeOverrides(
			DataNode.Effective<T> node);

	StructuredDataSource input();
}
