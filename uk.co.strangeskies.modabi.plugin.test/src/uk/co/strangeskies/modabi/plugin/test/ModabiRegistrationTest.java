/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.plugin.test.
 *
 * uk.co.strangeskies.modabi.plugin.test is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.plugin.test is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.plugin.test.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi.plugin.test;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.SchemaManager;
import uk.co.strangeskies.modabi.plugin.ModabiRegistration;
import uk.co.strangeskies.modabi.plugin.RegistrationContext;
import uk.co.strangeskies.utilities.Log;
import uk.co.strangeskies.utilities.classpath.Attribute;
import uk.co.strangeskies.utilities.collection.MultiHashMap;
import uk.co.strangeskies.utilities.collection.MultiMap;

public class ModabiRegistrationTest {
	private static final String RESOURCE_LOCATION = "/META-INF/schemata/";
	protected static final String XML_POSTFIX = ".xml";

	private BundleContext getBundleContext() {
		return FrameworkUtil.getBundle(this.getClass()).getBundleContext();
	}

	private <T> T getService(Class<T> clazz) {
		try {
			BundleContext context = getBundleContext();

			ServiceTracker<T, T> serviceTracker = new ServiceTracker<>(context, clazz, null);
			serviceTracker.open();
			try {
				return serviceTracker.waitForService(1000);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		} catch (Throwable t) {
			t.printStackTrace();
			throw t;
		}
	}

	public RegistrationContext createSimpleContext() {
		return createSimpleContext(emptySet(), emptySet());
	}

	public RegistrationContext createSimpleContext(Collection<String> availableSources,
			Collection<String> availableDependencies) {
		return new RegistrationContext() {
			private final Set<String> sources = availableSources.stream().map(s -> RESOURCE_LOCATION + s + XML_POSTFIX)
					.collect(Collectors.toSet());
			private final Set<QualifiedName> dependendencies = availableDependencies.stream()
					.map(s -> new QualifiedName(s, Schema.MODABI_NAMESPACE)).collect(Collectors.toSet());

			/*
			 * Services
			 */
			private final SchemaManager manager = getService(SchemaManager.class);
			private final Log log = getService(Log.class);
			private MultiMap<String, Attribute, Set<Attribute>> attributes = new MultiHashMap<>(HashSet::new);

			@Override
			public void log(Level level, String message) {
				log.log(level, message);
			}

			@Override
			public void log(Level level, String message, Throwable exception) {
				log.log(level, message, exception);
			}

			@Override
			public Set<String> sources() {
				return sources;
			}

			@Override
			public SchemaManager schemaManager() {
				return manager;
			}

			@Override
			public InputStream openSource(String sourceLocation) throws Exception {
				return getBundleContext().getBundle().getResource(sourceLocation).openStream();
			}

			@Override
			public InputStream openDependency(QualifiedName name) throws Exception {
				return openSource(RESOURCE_LOCATION + name.getName() + XML_POSTFIX);
			}

			@Override
			public String formatId() {
				return "xml";
			}

			@Override
			public ClassLoader classLoader() {
				return getClass().getClassLoader();
			}

			@Override
			public Set<QualifiedName> availableDependencies() {
				return dependendencies;
			}

			@Override
			public void addAttributes(String attributeName, List<Attribute> attributes) {
				this.attributes.addAll(attributeName, attributes);
			}

			public MultiMap<String, Attribute, Set<Attribute>> getAttributes() {
				return attributes;
			}
		};
	}

	private RegistrationContext runSimpleTest(RegistrationContext context) {
		new ModabiRegistration().registerSchemata(context);
		return context;
	}

	@Test
	public void createEmptyContextTest() {
		createSimpleContext();
	}

	@Test
	public void createModabiRegistrationTest() {
		new ModabiRegistration();
	}

	@Test
	public void loadEmpty() {
		runSimpleTest(createSimpleContext(asList("Empty"), emptySet()));
	}

	@Test(timeout = 2000)
	public void loadEmptyDependent() {
		runSimpleTest(createSimpleContext(asList("EmptyDep"), asList("Empty")));
	}

	@Test(timeout = 2000, expected = SchemaException.class)
	public void failLoadEmptyDependent() {
		runSimpleTest(createSimpleContext(asList("EmptyDep"), emptySet()));
	}

	@Test
	public void loadTypes() {
		runSimpleTest(createSimpleContext(asList("Types"), emptySet()));
	}

	@Test(timeout = 2000)
	public void loadTypesDependent() {
		runSimpleTest(createSimpleContext(asList("TypesDep"), asList("Types")));
	}

	@Test(timeout = 2000, expected = SchemaException.class)
	public void failLoadTypesDependent() {
		runSimpleTest(createSimpleContext(asList("TypesDep"), emptySet()));
	}

	@Test(timeout = 2000, expected = SchemaException.class)
	public void missingDependency() {
		runSimpleTest(createSimpleContext(asList("MissingDep"), emptySet()));
	}

	@Test(timeout = 2000, expected = SchemaException.class)
	public void namedMissingDependency() {
		runSimpleTest(createSimpleContext(asList("MissingDep"), asList("Missing")));
	}
}
