package uk.co.strangeskies.modabi.model.impl;

import java.util.List;

import uk.co.strangeskies.modabi.data.TerminatingDataTarget;
import uk.co.strangeskies.modabi.model.AbstractModel;
import uk.co.strangeskies.modabi.model.Model;

public interface UnbindingChildContext {
	TerminatingDataTarget property(String id);

	TerminatingDataTarget simpleElement(String id);

	TerminatingDataTarget content();

	TerminatingDataTarget getOpenDataTarget();

	void endData();

	Object getUnbindingTarget();

	void pushTarget(Object target);

	void popTarget();

	<U> List<Model<? extends U>> getMatchingModels(AbstractModel<U> element,
			Class<?> dataClass);
}
