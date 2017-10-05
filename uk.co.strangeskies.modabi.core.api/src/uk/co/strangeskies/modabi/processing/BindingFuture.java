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

import static java.util.Collections.singleton;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static uk.co.strangeskies.modabi.ModabiException.MESSAGES;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import uk.co.strangeskies.modabi.Binding;
import uk.co.strangeskies.modabi.ModabiException;
import uk.co.strangeskies.modabi.schema.BindingPoint;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.observable.ColdObservable;
import uk.co.strangeskies.observable.Observable;

public interface BindingFuture<T> extends Future<Binding<? extends T>> {
  Future<Model<? super T>> getModelFuture();

  Future<BindingPoint<? super T>> getBindingPointFuture();

  Blocks blocks();

  Observable<Binding<T>> observable();

  default T resolve() {
    try {
      return get().getData();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  default T resolve(long timeout, TimeUnit unit) {
    try {
      return get(timeout, unit).getData();
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      throw new RuntimeException(e);
    }
  }

  default Binding<? extends T> getNow() throws InterruptedException, ExecutionException {
    if (!isDone() && cancel(true))
      throw new ModabiException(MESSAGES.missingDependencies(blocks()));

    return get();
  }

  default T resolveNow() throws InterruptedException, ExecutionException {
    return getNow().getData();
  }

  static <U> BindingFuture<U> forBinding(Binding<U> binding) {
    return new BindingFuture<U>() {
      @Override
      public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
      }

      @Override
      public boolean isCancelled() {
        return false;
      }

      @Override
      public boolean isDone() {
        return true;
      }

      @Override
      public Binding<U> get() {
        return binding;
      }

      @Override
      public Observable<Binding<U>> observable() {
        return new ColdObservable<>(singleton(binding));
      }

      @Override
      public Binding<U> get(long timeout, TimeUnit unit) {
        return binding;
      }

      @Override
      public Future<Model<? super U>> getModelFuture() {
        return completedFuture(binding.getModel());
      }

      @Override
      public Future<BindingPoint<? super U>> getBindingPointFuture() {
        return completedFuture(binding.getBindingPoint());
      }

      @Override
      public Blocks blocks() {
        return new Blocks() {};
      }
    };
  }
}
