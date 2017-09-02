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
package uk.co.strangeskies.modabi.processing;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import uk.co.strangeskies.collection.multimap.MultiHashMap;
import uk.co.strangeskies.collection.multimap.MultiMap;
import uk.co.strangeskies.modabi.Binding;
import uk.co.strangeskies.modabi.schema.BindingPoint;
import uk.co.strangeskies.observable.HotObservable;
import uk.co.strangeskies.observable.Observable;

/**
 * The bindings which have been made so far during some processing operation.
 * <p>
 * These bindings are indexed by the exact node the occur at.
 * <p>
 * Though they are not indexed by the model (or set of models) they satisfy, it
 * is still possible to quickly query which bindings satisfy a given model, as
 * for each model we can precompute the set of the models satisfied by each
 * node within that model (or potentially satisfied in the case of extensible
 * nodes).
 * 
 * @author Elias N Vasylenko
 */
public class ProcessedBindings {
  public final MultiMap<BindingPoint<?>, Binding<?>, Set<Binding<?>>> bindingPointBindings;

  private final HotObservable<Binding<?>> listeners;

  public ProcessedBindings() {
    bindingPointBindings = new MultiHashMap<>(HashSet::new);

    listeners = new HotObservable<>();
  }

  private <T> void addCapture(Binding<T> binding) {
    bindingPointBindings.add(binding.getBindingPoint(), binding);

    listeners.next(binding);
  }

  public synchronized void add(Binding<?>... bindings) {
    add(asList(bindings));
  }

  public synchronized void add(Collection<? extends Binding<?>> bindings) {
    for (Binding<?> binding : bindings)
      addCapture(binding);
  }

  @SuppressWarnings("unchecked")
  public synchronized <T> Set<T> getBindings(BindingPoint<T> node) {
    return bindingPointBindings.getOrDefault(node, emptySet()).stream().map(t -> (T) t).collect(
        toSet());
  }

  @SuppressWarnings("unchecked")
  public synchronized <T> Observable<Binding<? extends T>> changes(BindingPoint<T> bindingPoint) {
    return listeners
        .filter(binding -> bindingPointBindings.get(bindingPoint).contains(binding))
        .map(n -> (Binding<? extends T>) n);
  }

  @Override
  public String toString() {
    return bindingPointBindings
        .values()
        .stream()
        .flatMap(Collection::stream)
        .collect(toSet())
        .toString();
  }
}
