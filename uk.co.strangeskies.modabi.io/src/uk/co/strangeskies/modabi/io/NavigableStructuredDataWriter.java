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
package uk.co.strangeskies.modabi.io;

import java.util.Optional;

import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;

public interface NavigableStructuredDataWriter
    extends NavigableStructuredDataReader, StructuredDataWriter {
  @Override
  NavigableStructuredDataWriter reset();

  @Override
  NavigableStructuredDataWriter readNextChild();

  @Override
  default NavigableStructuredDataWriter skipNextChild() {
    return (NavigableStructuredDataWriter) NavigableStructuredDataReader.super.skipNextChild();
  }

  @Override
  default NavigableStructuredDataWriter skipChildren() {
    return (NavigableStructuredDataWriter) NavigableStructuredDataReader.super.skipChildren();
  }

  @Override
  StructuredDataPosition getPosition();

  @Override
  NavigableStructuredDataWriter endChild();

  @Override
  NavigableStructuredDataWriter registerDefaultNamespaceHint(Namespace namespace);

  @Override
  NavigableStructuredDataWriter registerNamespaceHint(Namespace namespace);

  @Override
  NavigableStructuredDataWriter addChild(QualifiedName name);

  @Override
  NavigableStructuredDataWriter writeProperty(QualifiedName name, String value);

  @Override
  NavigableStructuredDataWriter setPrimaryProperty(QualifiedName name);

  @Override
  NavigableStructuredDataWriter comment(String comment);

  @Override
  Optional<QualifiedName> getNextChild();
}
