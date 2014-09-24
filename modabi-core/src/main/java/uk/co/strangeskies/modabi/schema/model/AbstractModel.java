package uk.co.strangeskies.modabi.schema.model;

import java.util.List;

import uk.co.strangeskies.modabi.schema.model.nodes.BindingNode;
import uk.co.strangeskies.utilities.PropertySet;

public interface AbstractModel<T, S extends AbstractModel<T, S, E>, E extends AbstractModel.Effective<T, S, E>>
		extends BindingNode<T, S, E> {
	interface Effective<T, S extends AbstractModel<T, S, E>, E extends AbstractModel.Effective<T, S, E>>
			extends AbstractModel<T, S, E>, BindingNode.Effective<T, S, E> {
		@Override
		List<Model.Effective<? super T>> baseModel();
	}

	@Override
	default PropertySet<S> propertySet() {
		return BindingNode.super.propertySet().add(AbstractModel::baseModel);
	}

	List<? extends Model<? super T>> baseModel();
}
