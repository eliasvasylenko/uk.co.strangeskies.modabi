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

import static java.util.function.Function.identity;
import static uk.co.strangeskies.observable.Observer.singleUse;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import uk.co.strangeskies.collection.SetTransformationView;
import uk.co.strangeskies.collection.observable.ObservableSetDecorator;
import uk.co.strangeskies.collection.observable.SynchronizedObservableSet;
import uk.co.strangeskies.observable.Disposable;
import uk.co.strangeskies.utility.IdentityProperty;

public abstract class NamedSet<N, T> extends ObservableSetDecorator<T> {
  private final Function<T, N> namingFunction;
  private final LinkedHashMap<N, T> elements;
  private final Object mutex;

  protected NamedSet(Function<T, N> namingFunction) {
    this(namingFunction, new LinkedHashMap<>());
  }

  private NamedSet(Function<T, N> namingFunction, LinkedHashMap<N, T> elements) {
    this(
        namingFunction,
        elements,
        SynchronizedObservableSet.over(
            new ObservableSetDecorator<>(
                new SetTransformationView<T, T>(elements.values(), identity()) {
                  @Override
                  public boolean add(T e) {
                    N name = namingFunction.apply(e);
                    if (elements.get(name) != null)
                      return false;

                    return elements.putIfAbsent(name, e) == null;
                  }

                  @Override
                  public boolean addAll(Collection<? extends T> elements) {
                    boolean changed = false;
                    for (T element : elements) {
                      changed = add(element) || changed;
                    }
                    return changed;
                  }
                })));
  }

  private NamedSet(
      Function<T, N> namingFunction,
      LinkedHashMap<N, T> elements,
      SynchronizedObservableSet<T> set) {
    super(set);

    this.namingFunction = namingFunction;
    this.elements = elements;
    this.mutex = set.getMutex();
  }

  public N nameOf(T element) {
    return namingFunction.apply(element);
  }

  protected Object getMutex() {
    return mutex;
  }

  public T get(N name) {
    synchronized (getMutex()) {
      return elements.get(name);
    }
  }

  public T waitForGet(N name) throws InterruptedException {
    return waitForGet(name, () -> {});
  }

  public T waitForGet(N name, Runnable onPresent) throws InterruptedException {
    return waitForGet(name, onPresent, -1);
  }

  public T waitForGet(N name, int timeoutMilliseconds) throws InterruptedException {
    return waitForGet(name, () -> {}, timeoutMilliseconds);
  }

  public T waitForGet(N name, Runnable onPresent, int timeoutMilliseconds)
      throws InterruptedException {
    IdentityProperty<T> result = new IdentityProperty<>();

    synchronized (getMutex()) {
      Disposable observation = changes().observe(singleUse(o -> c -> {
        synchronized (getMutex()) {
          if (result.get() != null) {
            o.cancel();
            return;
          }

          for (T element : c.added()) {
            if (nameOf(element).equals(name)) {
              onPresent.run();

              result.set(element);
              getMutex().notifyAll();

              o.cancel();
              return;
            }
          }
        }
      }));

      T element = get(name);

      if (element != null) {
        onPresent.run();

        result.set(element);
        observation.cancel();
      } else {
        try {
          do {
            if (timeoutMilliseconds < 0) {
              getMutex().wait();
            } else {
              getMutex().wait(timeoutMilliseconds);
            }
          } while (result.get() == null);
        } catch (InterruptedException e) {
          if (result.get() == null) {
            observation.cancel();
            throw e;
          }
        }
      }
    }

    return result.get();
  }

  public Map<N, T> getElements() {
    return new HashMap<>(this.elements);
  }

  @Override
  public String toString() {
    return getElements().toString();
  }
}
