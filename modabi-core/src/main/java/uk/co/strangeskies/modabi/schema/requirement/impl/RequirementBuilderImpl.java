package uk.co.strangeskies.modabi.schema.requirement.impl;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.requirement.Provision;
import uk.co.strangeskies.modabi.schema.requirement.Requirement;
import uk.co.strangeskies.modabi.schema.requirement.RequirementBuilder;

public class RequirementBuilderImpl implements RequirementBuilder {
	@Override
	public <T> Configurator<T> configure(QualifiedName name,
			Class<T> requiredClass) {
		return new Configurator<T>() {
			private Set<Provision> provisions;

			@Override
			public Requirement<T> create() {
				return new Requirement<T>() {
					@Override
					public QualifiedName getName() {
						return name;
					}

					@Override
					public Class<?> getRequiredClass() {
						return requiredClass;
					}

					@Override
					public Set<Provision> getProvisions() {
						return provisions;
					}
				};
			}

			@Override
			public Provision.Configurator addProvision(Class<?> providedClass,
					String providingMethod, Class<?>... providingMethodParameters) {
				return createProvision(p -> provisions.add(p), requiredClass,
						providedClass, providingMethod, providingMethodParameters);
			}

			private Provision.Configurator createProvision(
					Consumer<Provision> result, Class<?> baseClass,
					Class<?> providedClass, String providingMethod,
					Class<?>... providingMethodParameters) {
				Method method;
				try {
					method = baseClass.getDeclaredMethod(providingMethod,
							providingMethodParameters);
				} catch (NoSuchMethodException | SecurityException e) {
					throw new SchemaException(e);
				}

				return new Provision.Configurator() {
					private Set<Provision> provisions = new HashSet<>();

					@Override
					public Provision create() {
						return new Provision() {
							@Override
							public Class<?> getProvidedClass() {
								return providedClass;
							}

							@Override
							public Method getProvidingMethod() {
								return method;
							}

							@Override
							public Set<Provision> getProvisions() {
								return provisions;
							}
						};
					}

					@Override
					public Provision.Configurator addProvision(Class<?> subProvidedClass,
							String providedMethod, Class<?>... providingMethodParameters) {
						return createProvision(p -> provisions.add(p), providedClass,
								subProvidedClass, providedMethod, providingMethodParameters);
					}
				};
			}
		};
	}
}
