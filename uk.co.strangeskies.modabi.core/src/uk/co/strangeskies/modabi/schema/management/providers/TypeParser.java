package uk.co.strangeskies.modabi.schema.management.providers;

import java.lang.reflect.ParameterizedType;

public interface TypeParser {
	ParameterizedType parse(String string);
}
