package uk.co.strangeskies.modabi.schema.requirement;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.function.Function;

import uk.co.strangeskies.utilities.factory.Factory;

public interface Provision {
	public interface Configurator extends Factory<Provision> {
		Provision.Configurator addProvision(Class<?> providedClass,
				String providedMethod, Class<?>... providingMethodParameters);

		default Configurator addProvision(
				Function<Configurator, Configurator> configurator,
				Class<?> providedClass, String providingMethod,
				Class<?>... providingMethodParameters) {
			configurator.apply(addProvision(providedClass, providingMethod)).create();
			return this;
		}
	}

	Class<?> getProvidedClass();

	Method getProvidingMethod();

	Set<Provision> getProvisions();
}
