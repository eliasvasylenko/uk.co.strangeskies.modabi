package uk.co.strangeskies.modabi.schema.processing;

public interface Provisions {
	public <T> T provide(Class<T> clazz);

	public boolean isProvided(Class<?> clazz);
}
