package uk.co.strangeskies.modabi.schema.processing.unbinding;

import java.util.List;

import uk.co.strangeskies.modabi.io.structured.StructuredDataTarget;
import uk.co.strangeskies.modabi.schema.node.ComplexNode;
import uk.co.strangeskies.modabi.schema.node.DataNode;
import uk.co.strangeskies.modabi.schema.node.model.Model;
import uk.co.strangeskies.modabi.schema.node.type.DataBindingType;
import uk.co.strangeskies.modabi.schema.processing.Provisions;

public interface UnbindingContext extends UnbindingState {
	Provisions provisions();

	<T> List<Model.Effective<T>> getMatchingModels(Class<T> dataClass);

	<T> List<Model.Effective<? extends T>> getMatchingModels(
			ComplexNode.Effective<T> element, Class<? extends T> dataClass);

	<T> List<DataBindingType.Effective<? extends T>> getMatchingTypes(
			DataNode.Effective<T> node, Class<?> dataClass);

	StructuredDataTarget output();
}
