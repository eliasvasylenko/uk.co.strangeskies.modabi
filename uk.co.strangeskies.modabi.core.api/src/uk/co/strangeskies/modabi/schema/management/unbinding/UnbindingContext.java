package uk.co.strangeskies.modabi.schema.management.unbinding;

import java.util.List;

import uk.co.strangeskies.modabi.io.structured.StructuredDataTarget;
import uk.co.strangeskies.reflection.TypeLiteral;
import uk.co.strangeskies.modabi.schema.management.Provisions;
import uk.co.strangeskies.modabi.schema.node.ComplexNode;
import uk.co.strangeskies.modabi.schema.node.DataNode;
import uk.co.strangeskies.modabi.schema.node.model.Model;
import uk.co.strangeskies.modabi.schema.node.type.DataBindingType;
import uk.co.strangeskies.utilities.collection.computingmap.ComputingMap;

public interface UnbindingContext extends UnbindingState {
	Provisions provisions();

	<T> List<Model.Effective<T>> getMatchingModels(TypeLiteral<T> dataClass);

	<T> ComputingMap<Model.Effective<? extends T>, ComplexNode.Effective<? extends T>> getComplexNodeOverrides(
			ComplexNode.Effective<T> element);

	<T> ComputingMap<DataBindingType.Effective<? extends T>, DataNode.Effective<? extends T>> getDataNodeOverrides(
			DataNode.Effective<T> node);

	StructuredDataTarget output();
}
