package uk.co.strangeskies.modabi.schema.node.building.configuration;

import java.util.Arrays;
import java.util.List;

import uk.co.strangeskies.modabi.schema.node.ComplexNode;
import uk.co.strangeskies.modabi.schema.node.model.Model;

public interface ComplexNodeConfigurator<T> extends
		AbstractModelConfigurator<ComplexNodeConfigurator<T>, ComplexNode<T>, T>,
		BindingChildNodeConfigurator<ComplexNodeConfigurator<T>, ComplexNode<T>, T> {
	@Override
	default <V extends T> ComplexNodeConfigurator<V> baseModel(
			@SuppressWarnings("unchecked") Model<? super V>... baseModel) {
		return baseModel(Arrays.asList(baseModel));
	}

	@Override
	<V extends T> ComplexNodeConfigurator<V> baseModel(
			List<? extends Model<? super V>> baseModel);

	@Override
	<V extends T> ComplexNodeConfigurator<V> dataClass(Class<V> dataClass);

	ComplexNodeConfigurator<T> inline(boolean inline);
}
