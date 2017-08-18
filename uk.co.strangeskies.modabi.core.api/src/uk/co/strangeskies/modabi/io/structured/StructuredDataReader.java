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
import java.util.Set;
import java.util.stream.Stream;

import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;

/**
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * TODO consider option of making ALL structure data interfaces derive from
 * reader! If we have a writer into an empty document that might not make sense
 * as a reader ... but at the same time isn't reading an empty document a
 * perfectly valid function to perform?
 * 
 * Think about how this affects the hierarchy when navigability comes into it.
 * 
 *
 * 
 * 
 * 
 * 
 * 
 * 
 * @author Elias N Vasylenko
 *
 */
public interface StructuredDataReader {
  public Namespace getDefaultNamespaceHint();

  public Set<Namespace> getNamespaceHints();

  public Stream<String> getComments();

  public StructuredDataReader readNextChild();

  public Optional<QualifiedName> getNextChild();

  public Set<QualifiedName> getProperties();

  public Optional<String> readProperty(QualifiedName name);

  public Optional<String> readContent();

  public default boolean skipNextChild() {
    boolean hasNext = getNextChild().isPresent();
    if (hasNext) {
      readNextChild();
      skipChildren();
      endChild();
    }
    return hasNext;
  }

  public default StructuredDataReader skipChildren() {
    while (skipNextChild()) {}
    return this;
  }

  /**
   * throws an exception if there are more children, so call skipChildren() first,
   * or call endChildEarly, if you want to ignore them.
   */
  public StructuredDataReader endChild();

  public StructuredDataPosition getPosition();

  public StructuredDataReader split();

  public NavigableStructuredDataReader buffer();
}
