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
package uk.co.strangeskies.modabi;

import static uk.co.strangeskies.modabi.ModabiException.MESSAGES;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

public abstract class NamedSet<N, T> {
  private final Function<T, N> namingFunction;
  private final Map<N, T> elements;

  protected NamedSet(Function<T, N> namingFunction) {
    this.namingFunction = namingFunction;
    this.elements = new LinkedHashMap<>();
  }

  protected Object getMutex() {
    return elements;
  }

  protected void add(T element) {
    synchronized (getMutex()) {
      N name = namingFunction.apply(element);
      if (elements.containsKey(name))
        throw new ModabiException(MESSAGES.cannotAcceptDuplicate(name));
      elements.put(name, element);
    }
  }

  protected void remove(T element) {
    synchronized (getMutex()) {
      N name = getName(element);
      if (name != null)
        elements.remove(name);
    }
  }

  public boolean containsName(N name) {
    synchronized (getMutex()) {
      return elements.containsKey(name);
    }
  }

  public N getName(T element) {
    synchronized (getMutex()) {
      N name = namingFunction.apply(element);
      return containsName(name) ? name : null;
    }
  }

  public Stream<N> getAllNames() {
    synchronized (getMutex()) {
      return elements.keySet().stream();
    }
  }

  public boolean contains(T element) {
    synchronized (getMutex()) {
      return containsName(namingFunction.apply(element));
    }
  }

  public T get(N name) {
    synchronized (getMutex()) {
      return elements.get(name);
    }
  }

  public Stream<T> getAll() {
    synchronized (getMutex()) {
      return new ArrayList<>(elements.values()).stream();
    }
  }

  @Override
  public String toString() {
    synchronized (getMutex()) {
      return elements.toString();
    }
  }
}
