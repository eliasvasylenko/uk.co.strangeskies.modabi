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
package uk.co.strangeskies.modabi.bnd;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.SchemaManager;
import uk.co.strangeskies.utilities.classpath.Attribute;

public interface RegistrationContext {
	void addAttributes(String attributeName, List<Attribute> attributes);

	default void addAttributes(String attributeName, Attribute... attributes) {
		addAttributes(attributeName, Arrays.asList(attributes));
	}

	String formatId();

	SchemaManager schemaManager();

	ClassLoader classLoader();

	Set<String> sources();

	InputStream openSource(String sourceLocation) throws Exception;

	Set<QualifiedName> availableDependencies();

	InputStream openDependency(QualifiedName name) throws Exception;
}
