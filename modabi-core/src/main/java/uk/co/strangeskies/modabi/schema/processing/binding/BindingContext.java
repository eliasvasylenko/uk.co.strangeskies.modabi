package uk.co.strangeskies.modabi.schema.processing.binding;

import java.util.List;

import uk.co.strangeskies.modabi.io.structured.StructuredDataSource;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.node.DataNode;
import uk.co.strangeskies.modabi.schema.node.model.Model;
import uk.co.strangeskies.modabi.schema.node.type.DataBindingType;
import uk.co.strangeskies.modabi.schema.processing.Provisions;

public interface BindingContext extends BindingState {
	Provisions provisions();

	Model.Effective<?> getModel(QualifiedName nextElement);

	<T> List<DataBindingType.Effective<? extends T>> getMatchingTypes(
			DataNode.Effective<T> node, Class<?> dataClass);

	StructuredDataSource input();
}
