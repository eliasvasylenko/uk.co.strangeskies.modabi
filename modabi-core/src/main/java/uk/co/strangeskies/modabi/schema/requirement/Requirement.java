package uk.co.strangeskies.modabi.schema.requirement;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.SchemaException;

public interface Requirement<T> {
	QualifiedName getName();

	Class<?> getRequiredClass();

	Set<Provision> getProvisions();

	default Collection<? extends Class<?>> getProvidedClasses() {
		Set<Class<?>> providedClasses = new HashSet<>();
		List<Provision> provisions = new ArrayList<>(getProvisions());
		for (int i = 0; i < provisions.size(); i++) {
			Provision provision = provisions.get(i);
			providedClasses.add(provision.getProvidedClass());
			provisions.addAll(provision.getProvisions());
		}
		return providedClasses;
	}

	default <U> U provideClass(T requirement, Class<U> providedClass) {
		Object object = requirement;
		for (Method method : getProvidingMethodChain(providedClass))
			try {
				object = method.invoke(object);
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				throw new SchemaException("Cannot provide class '" + providedClass
						+ "' with requirement '" + getRequiredClass() + "'.", e);
			}
		return providedClass.cast(object);
	}

	default List<Method> getProvidingMethodChain(Class<?> providedClass) {
		Map<Provision, Provision> providerTree = new HashMap<>();
		List<Provision> provisions = new ArrayList<>(getProvisions());
		for (int i = 0; i < provisions.size(); i++) {
			Provision provision = provisions.get(i);

			if (provision.getProvidedClass().equals(providedClass)) {
				List<Method> providingMethodChain = new ArrayList<>();
				providingMethodChain.add(provision.getProvidingMethod());
				while ((provision = providerTree.get(provision)) != null)
					providingMethodChain.add(0, provision.getProvidingMethod());

				return providingMethodChain;
			}

			provisions.addAll(provision.getProvisions());
			for (Provision provided : provision.getProvisions())
				providerTree.put(provided, provision);
		}

		throw new SchemaException("Cannot provide class '" + providedClass
				+ "' with requirement '" + getRequiredClass() + "'.");
	}
}
