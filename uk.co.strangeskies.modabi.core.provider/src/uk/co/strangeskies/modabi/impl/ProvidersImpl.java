/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.core.provider.
 *
 * uk.co.strangeskies.modabi.core.provider is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.core.provider is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.core.provider.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi.impl;

import java.util.HashSet;
import java.util.stream.Stream;

import uk.co.strangeskies.collection.observable.ObservableSet;
import uk.co.strangeskies.collection.observable.ObservableSetDecorator;
import uk.co.strangeskies.modabi.Provider;
import uk.co.strangeskies.modabi.Providers;

public final class ProvidersImpl extends ObservableSetDecorator<Providers, Provider>
    implements Providers {
  private final ProvidersImpl parent;

  protected ProvidersImpl(ProvidersImpl parent) {
    super(ObservableSet.over(new HashSet<Provider>()).synchronizedView());
    this.parent = parent;
  }

  public ProvidersImpl() {
    this(null);
  }

  protected Stream<Provider> visiblePriovidersStream() {
    if (parent == null)
      return stream();
    else
      return Stream.concat(stream(), parent.visiblePriovidersStream());
  }

  @Override
  public ProvidersImpl copy() {
    ProvidersImpl parentCopy = parent != null ? parent.copy() : null;
    ProvidersImpl copy = new ProvidersImpl(parentCopy);
    copy.addAll(this);
    return copy;
  }
}
