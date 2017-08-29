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

import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Function;

import uk.co.strangeskies.modabi.QualifiedName;

/**
 * {@link BindingBlock}s are intended to signal that a binding thread is waiting
 * for resources or dependencies to be made available by some external process.
 * Blocks should not be made if the resource is already available, even if
 * released immediately.
 * <p>
 * This class contains methods to create blocks over {@link Function}s from, and
 * {@link Consumer}s of {@link BindingBlock}. The block is created, then the
 * given function or consumer should fetch the resource, or wait for it to be
 * available.
 * <p>
 * Any {@link BindingBlock} which is currently blocking will prevent completion
 * of the owning {@link BindingFuture} until the given runnable process
 * completes, or until invocation of {@link BindingBlock#complete()}. The
 * process will be completed on the invoking thread, so invocation returns only
 * after the supplier completes execution and the block is lifted.
 * <p>
 * Unless otherwise specified to be internal, blocks may be satisfied by
 * external processes, so if a block is still in place when binding otherwise
 * completes, with no internal processing threads remaining unblocked, then the
 * binding will simply wait until the dependency becomes available.
 * <p>
 * The thread which the given blocking process is invoked on will always
 * terminate after invocation, and after releasing the block.
 * 
 * @author Elias N Vasylenko
 *
 */
public interface BindingBlocker extends BindingBlocks {
  /**
   * Create a block on a resource which can be uniquely identified by the given
   * namespace and id.
   * <p>
   * The invoking thread does not immediately wait on the block, and so the
   * invocation returns immediately.
   * 
   * @param namespace
   *          The namespace of the pending dependency
   * @param id
   *          The id of the pending dependency
   * @param internal
   *          Whether the block should only allow internal resolution, as opposed
   *          to possible satisfaction via external sources
   * @return
   */
  BindingBlock block(QualifiedName namespace, Object id, boolean internal);

  public Set<Thread> getParticipatingThreads();

  /**
   * Register a thread as a participant in the binding/unbinding process. This
   * helps the processor determine whether there is unblocked activity, or
   * otherwise detect a deadlock or unsatisfied dependency.
   * 
   * @param processingThread
   *          The thread participating in processing
   */
  void addParticipatingThread(Thread processingThread);

  /**
   * Register the current thread as a participant in the binding/unbinding
   * process. This helps the processor determine whether there is unblocked
   * activity, or otherwise detect a deadlock or unsatisfied dependency.
   */
  default void addParticipatingThread() {
    addParticipatingThread(Thread.currentThread());
  }

  public void complete() throws InterruptedException, ExecutionException;
}
