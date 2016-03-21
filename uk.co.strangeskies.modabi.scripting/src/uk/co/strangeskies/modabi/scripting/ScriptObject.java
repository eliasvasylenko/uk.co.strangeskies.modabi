package uk.co.strangeskies.modabi.scripting;

import uk.co.strangeskies.reflection.Reified;

public interface ScriptObject<T> extends Reified<ScriptObject<T>> {
	String getLanguage();

	String getObjectName();

	String getScript();

	String getResource();

	@SuppressWarnings("unchecked")
	default T cast() {
		return (T) this;
	}
}
