package uk.co.strangeskies.modabi.schema.management.providers;

import java.lang.reflect.ParameterizedType;

public interface TypeComposer {
	String compose(ParameterizedType type);
}
