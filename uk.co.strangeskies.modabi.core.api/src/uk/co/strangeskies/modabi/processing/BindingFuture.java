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

import static uk.co.strangeskies.modabi.ModabiException.MESSAGES;

import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import uk.co.strangeskies.modabi.Binding;
import uk.co.strangeskies.modabi.ModabiException;
import uk.co.strangeskies.modabi.schema.Model;

public interface BindingFuture<T> extends Future<Binding<T>> {
  Future<Model<? super T>> getModelFuture();

  BindingBlocks blocks();

  @Override
  Binding<T> get();

  @Override
  Binding<T> get(long timeout, TimeUnit unit);

  default T resolve() {
    return get().getData();
  }

  default T resolve(long timeout) {
    return get(timeout, TimeUnit.MILLISECONDS).getData();
  }

  default Binding<T> getNow() {
    BindingBlocks blockingBindings = blocks();

    if (!isDone() && cancel(true))
      throw new ModabiException(MESSAGES.missingDependencies(blockingBindings));

    return get();
  }

  default T resolveNow() {
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
      public Binding<U> get(long timeout, TimeUnit unit) {
        return binding;
      }

      @Override
      public Future<Model<? super U>> getModelFuture() {
        FutureTask<Model<? super U>> modelFuture = new FutureTask<>(binding::getModel);
        modelFuture.run();
        return modelFuture;
      }

      @Override
      public BindingBlocks blocks() {
        return BindingBlocks.NON_BLOCKING;
      }
    };
  }
}
