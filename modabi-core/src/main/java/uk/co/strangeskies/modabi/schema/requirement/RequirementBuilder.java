package uk.co.strangeskies.modabi.schema.requirement;

import java.util.function.Function;

import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.utilities.factory.Factory;

public interface RequirementBuilder {
	public interface Configurator<T> extends Factory<Requirement<T>> {
		Provision.Configurator addProvision(Class<?> providedClass,
				String providingMethod, Class<?>... providingMethodParameters);

		default Configurator<T> addProvision(
				Function<Provision.Configurator, Provision.Configurator> configurator,
				Class<?> providedClass, String providingMethod,
				Class<?>... providingMethodParameters) {
			configurator.apply(
					addProvision(providedClass, providingMethod,
							providingMethodParameters)).create();
			return this;
		}
	}

	<T> Configurator<T> configure(QualifiedName name, Class<T> requiredClass);
}
