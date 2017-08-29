/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.core.api.
 *
 * uk.co.strangeskies.modabi.core.api is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.core.api is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.core.api.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi.io.structured;

import java.util.Optional;
import java.util.stream.Stream;

import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;

public interface StructuredDataReader {
  Namespace getDefaultNamespaceHint();

  Stream<Namespace> getNamespaceHints();

  Stream<String> getComments();

  StructuredDataReader readNextChild();

  QualifiedName getName();

  Optional<QualifiedName> getNextChild();

  Stream<QualifiedName> getProperties();

  Optional<String> readProperty(QualifiedName name);

  Optional<String> readPrimaryProperty();

  default StructuredDataReader skipNextChild() {
    readNextChild();
    skipChildren();
    endChild();
    return this;
  }

  default StructuredDataReader skipChildren() {
    while (getNextChild().isPresent())
      skipNextChild();
    return this;
  }

  /**
   * throws an exception if there are more children, so call skipChildren() first,
   * or call endChildEarly, if you want to ignore them.
   */
  StructuredDataReader endChild();

  StructuredDataPosition getPosition();

  StructuredDataReader split();

  NavigableStructuredDataReader buffer();
}
