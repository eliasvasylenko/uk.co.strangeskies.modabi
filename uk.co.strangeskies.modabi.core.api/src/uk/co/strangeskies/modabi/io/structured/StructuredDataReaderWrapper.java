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

public interface StructuredDataReaderWrapper extends StructuredDataReader {
  StructuredDataReader getComponent();

  @Override
  default Namespace getDefaultNamespaceHint() {
    return getComponent().getDefaultNamespaceHint();
  }

  @Override
  default Stream<Namespace> getNamespaceHints() {
    return getComponent().getNamespaceHints();
  }

  @Override
  default Stream<String> getComments() {
    return getComponent().getComments();
  }

  @Override
  default StructuredDataReader readNextChild() {
    getComponent();
    return this;
  }

  @Override
  default Optional<QualifiedName> getNextChild() {
    return getComponent().getNextChild();
  }

  @Override
  default Stream<QualifiedName> getProperties() {
    return getComponent().getProperties();
  }

  @Override
  default Optional<String> readProperty(QualifiedName name) {
    return getComponent().readProperty(name);
  }

  @Override
  default Optional<String> readPrimaryProperty() {
    return getComponent().readPrimaryProperty();
  }

  @Override
  default StructuredDataReader endChild() {
    getComponent().endChild();
    return this;
  }

  @Override
  default StructuredDataPosition getPosition() {
    return getComponent().getPosition();
  }

  @Override
  StructuredDataReader split();

  @Override
  NavigableStructuredDataReader buffer();
}
