package uk.co.strangeskies.modabi.processing;

import java.util.List;

import uk.co.strangeskies.modabi.data.io.structured.StructuredOutput;
import uk.co.strangeskies.modabi.model.AbstractModel;
import uk.co.strangeskies.modabi.model.Model;

public interface UnbindingContext<T> {
	<U> List<Model<? extends U>> getMatchingModels(AbstractModel<U> element,
			Class<?> dataClass);

	StructuredOutput output();

	T data();
}
