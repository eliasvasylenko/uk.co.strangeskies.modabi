/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.plugin.
 *
 * uk.co.strangeskies.modabi.plugin is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.plugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.plugin.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi.plugin;

import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import uk.co.strangeskies.log.Log;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.SchemaManager;
import uk.co.strangeskies.reflection.resource.Attribute;

public interface RegistrationContext {
  void addAttributes(String attributeName, List<Attribute> attributes);

  default void addAttributes(String attributeName, Attribute... attributes) {
    addAttributes(attributeName, Arrays.asList(attributes));
  }

  Log getLog();

  String getFormatId();

  SchemaManager getSchemaManager();

  ClassLoader getClassLoader();

  Stream<String> getSources();

  ReadableByteChannel openSource(String sourceLocation) throws Exception;

  Stream<QualifiedName> getAvailableDependencies();

  ReadableByteChannel openDependency(QualifiedName name) throws Exception;
}
