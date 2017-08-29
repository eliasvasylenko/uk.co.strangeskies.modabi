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

import static uk.co.strangeskies.modabi.ModabiException.MESSAGES;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import uk.co.strangeskies.modabi.ModabiException;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.processing.BindingBlock;
import uk.co.strangeskies.modabi.processing.BindingBlockEvent;
import uk.co.strangeskies.observable.HotObservable;
import uk.co.strangeskies.observable.Observable;

public class BindingBlockImpl implements BindingBlock {
  private final HotObservable<BindingBlockEvent> blockEvents = new HotObservable<>();

  private final QualifiedName namespace;
  private final Object id;
  private final boolean internal;

  private boolean complete = false;
  private Throwable failure = null;

  public BindingBlockImpl(QualifiedName namespace, Object id, boolean internal) {
    this.namespace = namespace;
    this.id = id;
    this.internal = internal;
  }

  void fireEvent(BindingBlockEvent.Type type) {
    blockEvents.next(new BindingBlockEvent() {
      @Override
      public BindingBlock block() {
        return BindingBlockImpl.this;
      }

      @Override
      public Type type() {
        return type;
      }

      @Override
      public Thread thread() {
        return Thread.currentThread();
      }
    });
  }

  @Override
  public Observable<BindingBlockEvent> events() {
    return blockEvents;
  }

  @Override
  public QualifiedName namespace() {
    return namespace;
  }

  @Override
  public Object id() {
    return id;
  }

  @Override
  public synchronized boolean isEnded() {
    return complete || failure != null;
  }

  @Override
  public synchronized boolean isSuccessful() {
    return complete;
  }

  @Override
  public synchronized Throwable getFailure() {
    return failure;
  }

  @Override
  public boolean isInternal() {
    return internal;
  }

  @Override
  public synchronized void complete() throws ExecutionException {
    if (failure != null) {
      throw new ExecutionException("Failed to resolve blocking dependency " + this, failure);
    }

    if (!complete) {
      complete = true;
      notifyAll();
      fireEvent(BindingBlockEvent.Type.ENDED);
    }
  }

  @Override
  public synchronized boolean fail(Throwable cause) {
    if (isEnded()) {
      return false;
    } else {
      failure = cause;

      if (failure == null) {
        failure = new ModabiException(MESSAGES.unknownBlockingError(this));
      }

      notifyAll();
      fireEvent(BindingBlockEvent.Type.ENDED);
      return true;
    }
  }

  @Override
  public synchronized void waitUntilComplete() throws InterruptedException, ExecutionException {
    if (isEnded()) {
      return;
    }

    fireEvent(BindingBlockEvent.Type.THREAD_BLOCKED);

    while (!isEnded()) {
      tryWait();
    }
  }

  @Override
  public synchronized void waitUntilComplete(long timeoutMilliseconds)
      throws InterruptedException, TimeoutException, ExecutionException {
    synchronized (this) {
      if (isEnded()) {
        return;
      }

      fireEvent(BindingBlockEvent.Type.THREAD_BLOCKED);

      long wait = 0;
      long startTime = System.currentTimeMillis();

      while (!isEnded()) {
        wait = timeoutMilliseconds + startTime - System.currentTimeMillis();
        if (wait < 0) {
          break;
        }
        tryWait();
      }

      if (wait < 0) {
        fireEvent(BindingBlockEvent.Type.THREAD_UNBLOCKED);
        throw new TimeoutException("Timed out waiting for blocking dependency " + this);
      }
    }
  }

  private void tryWait() throws ExecutionException, InterruptedException {
    try {
      wait();
    } catch (InterruptedException e) {
      if (failure == null) {
        throw e;
      }
    }
    if (failure != null) {
      throw new ExecutionException("Failed to resolve blocking dependency " + this, failure);
    }
  }

  @Override
  public String toString() {
    return "(" + namespace + ", " + id + ", " + (internal ? "internal" : "external") + ")";
  }
}
