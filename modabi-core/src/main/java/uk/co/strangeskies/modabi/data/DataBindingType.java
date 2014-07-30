package uk.co.strangeskies.modabi.data;

import java.util.List;

import uk.co.strangeskies.modabi.model.nodes.BindingNode;
import uk.co.strangeskies.modabi.model.nodes.DataNode;

public interface DataBindingType<T> extends
		BindingNode<T, DataBindingType.Effective<T>> {
	interface Effective<T> extends DataBindingType<T>,
			BindingNode.Effective<T, Effective<T>> {
		List<DataNode.Effective<?>> providedUnbindingMethodParameters();
	}

	String getName();

	@Override
	default String getId() {
		return getName();
	}

	Boolean isAbstract();

	Boolean isPrivate();

	DataBindingType<? super T> baseType();

	List<String> providedUnbindingMethodParameterNames();
}
