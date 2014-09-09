package uk.co.strangeskies.modabi.schema.requirement;

import java.util.HashSet;
import java.util.Set;

public interface Requirements {
	Set<Requirement<?>> getRequirements();

	default Set<Class<?>> getProvidedClasses() {
		Set<Class<?>> providedClasses = new HashSet<>();
		for (Requirement<?> requirement : getRequirements())
			providedClasses.addAll(requirement.getProvidedClasses());
		return providedClasses;
	}

	default Requirement<?> getProvidingRequirement(Class<?> providedClass) {
		for (Requirement<?> requirement : getRequirements())
			if (requirement.getProvidedClasses().contains(providedClass))
				return requirement;
		throw new IllegalArgumentException();
	}
}
