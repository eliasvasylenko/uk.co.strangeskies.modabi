package uk.co.strangeskies.modabi.processing;

import java.util.List;

import uk.co.strangeskies.modabi.data.TerminatingDataSink;
import uk.co.strangeskies.modabi.model.AbstractModel;
import uk.co.strangeskies.modabi.model.Model;

public interface UnbindingContext {
	public TerminatingDataSink property(String id);

	public TerminatingDataSink simpleElement(String id);

	public TerminatingDataSink content();

	public void endData();

	public void beginElement(String id);

	public void endElement();

	public <T> List<Model<? extends T>> getMatchingModels(
			AbstractModel<T> element, Class<?> dataClass);
}
