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

import java.util.List;
import java.util.Set;

import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;

public class StructuredDataReaderWrapper implements StructuredDataReader {
  private StructuredDataReader component;

  public StructuredDataReaderWrapper(StructuredDataReader component) {
    this.component = component;
  }

  protected StructuredDataReader getComponent() {
    return component;
  }

  @Override
  public Namespace getDefaultNamespaceHint() {
    currentState().assertValid(StructuredDataState.UNSTARTED, StructuredDataState.ELEMENT_START);
    return getComponent().getDefaultNamespaceHint();
  }

  @Override
  public Set<Namespace> getNamespaceHints() {
    currentState().assertValid(StructuredDataState.UNSTARTED, StructuredDataState.ELEMENT_START);
    return getComponent().getNamespaceHints();
  }

  @Override
  public List<String> getComments() {
    currentState().assertValid(
        StructuredDataState.UNSTARTED,
        StructuredDataState.ELEMENT_START,
        StructuredDataState.POPULATED_ELEMENT);
    return getComponent().getComments();
  }

  @Override
  public QualifiedName readNextChild() {
    enterState(StructuredDataState.ELEMENT_START);
    return getComponent().readNextChild();
  }

  @Override
  public String readProperty(QualifiedName name) {
    return getComponent().readProperty(name);
  }

  @Override
  public String readContent() {
    return getComponent().readContent();
  }

  @Override
  public StructuredDataReader endChild() {
    if (index().isEmpty())
      enterState(StructuredDataState.FINISHED);
    else
      enterState(StructuredDataState.POPULATED_ELEMENT);
    getComponent().endChild();

    return this;
  }

  @Override
  public QualifiedName peekNextChild() {
    return getComponent().peekNextChild();
  }

  @Override
  public Set<QualifiedName> getProperties() {
    return getComponent().getProperties();
  }

  @Override
  public boolean hasNextChild() {
    return getComponent().hasNextChild();
  }

  @Override
  public List<Integer> index() {
    return getComponent().index();
  }

  @Override
  public StructuredDataReader split() {
    return new StructuredDataReaderWrapper(getComponent().split());
  }

  @Override
  public NavigableStructuredDataReader buffer() {
    return new NavigableStructuredDataSourceWrapper(getComponent().buffer());
  }
}
