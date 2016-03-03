/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.bnd.
 *
 * uk.co.strangeskies.modabi.bnd is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.bnd is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.bnd.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi.plugin.test;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.junit.Test;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.SchemaManager;
import uk.co.strangeskies.modabi.plugin.ModabiRegistration;
import uk.co.strangeskies.modabi.plugin.RegistrationContext;
import uk.co.strangeskies.utilities.classpath.Attribute;

public class ModabiRegistrationTest {
	public RegistrationContext createSimpleContext() {
		return createSimpleContext(Collections.emptySet(), Collections.emptySet());
	}

	public RegistrationContext createSimpleContext(Collection<? extends QualifiedName> availableSources,
			Collection<? extends QualifiedName> availableDependencies) {
		return new RegistrationContext() {
			private final Map<String, QualifiedName> sources = availableSources.stream()
					.collect(toMap(Objects::toString, identity()));
			private final Set<QualifiedName> dependendencies = new HashSet<>(availableSources);

			@Override
			public void log(Level level, String message) {
				// TODO Auto-generated method stub

			}

			@Override
			public Set<String> sources() {
				return sources.keySet();
			}

			@Override
			public SchemaManager schemaManager() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public InputStream openSource(String sourceLocation) throws Exception {
				return openDependency(sources.get(sourceLocation));
			}

			@Override
			public InputStream openDependency(QualifiedName name) throws Exception {
				// TODO Auto-generated method stub
				return null;
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
				// TODO Auto-generated method stub

			}
		};
	}

	@Test
	public void createEmptyContextTest() {
		createSimpleContext();
	}

	@Test
	public void createModabiRegistrationTest() {
		new ModabiRegistration(createSimpleContext());
	}
}
