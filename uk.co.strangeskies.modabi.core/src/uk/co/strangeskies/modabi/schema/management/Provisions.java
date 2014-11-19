package uk.co.strangeskies.modabi.schema.management;

public interface Provisions {
	public <T> T provide(Class<T> clazz);

	public boolean isProvided(Class<?> clazz);
}
