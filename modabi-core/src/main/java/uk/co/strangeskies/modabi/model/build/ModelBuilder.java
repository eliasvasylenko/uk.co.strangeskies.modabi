package uk.co.strangeskies.modabi.model.build;

import uk.co.strangeskies.gears.utilities.factory.Builder;
import uk.co.strangeskies.modabi.model.Model;

public interface ModelBuilder extends
		Builder<ModelConfigurator<Object>, Model<Object>> {
}
