package uk.co.strangeskies.modabi.model;

import java.util.List;

import uk.co.strangeskies.modabi.model.nodes.BindingNode;
import uk.co.strangeskies.modabi.processing.SchemaProcessingContext;

public interface AbstractModel<T> extends BindingNode<T> {
	public Boolean isAbstract();

	public List<Model<? super T>> getBaseModel();

	@Override
	default void process(SchemaProcessingContext context) {
		context.accept(this);
	}
}
