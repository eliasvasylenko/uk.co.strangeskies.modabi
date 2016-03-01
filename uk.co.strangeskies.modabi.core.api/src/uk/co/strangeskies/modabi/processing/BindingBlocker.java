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

import java.util.function.Consumer;
import java.util.function.Function;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.io.DataSource;

/**
 * {@link BindingBlock}s are intended to signal that a binding thread is waiting for
 * resources or dependencies to be made available by some external process.
 * Blocks should not be made if the resource is already available, even if
 * released immediately.
 * <p>
 * This class contains methods to create blocks over {@link Function}s from, and
 * {@link Consumer}s of {@link BindingBlock}. The block is created, then the given
 * function or consumer should fetch the resource, or wait for it to be
 * available.
 * <p>
 * Any {@link BindingBlock} which is currently blocking will prevent completion of the
 * owning {@link BindingFuture} until the given runnable process completes, or
 * until invocation of {@link BindingBlock#complete()}. The process will be completed
 * on the invoking thread, so invocation returns only after the supplier
 * completes execution and the block is lifted.
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
public interface BindingBlocker {
	/**
	 * Create a block over the given {@link Function} blocking process, then wait
	 * until the resource is available.
	 * 
	 * @param blockingSupplier
	 *          The runnable which will block until its execution completes
	 * @param namespace
	 *          The namespace of the blocking resource
	 * @param id
	 *          The id of the blocking resource
	 * @return The new thread which is performing the blocking resource fetching
	 */
	<T> T blockAndWaitFor(Function<BindingBlock, ? extends T> blockingSupplier, QualifiedName namespace, DataSource id);

	/**
	 * Create a block over the given {@link Consumer} blocking process, then wait
	 * until the resource is available.
	 * 
	 * @param blockingSupplier
	 *          The runnable which will block until its execution completes
	 * @param namespace
	 *          The namespace of the blocking resource
	 * @param id
	 *          The id of the blocking resource
	 * @return The new thread which is performing the blocking resource fetching
	 */
	default void blockAndWaitFor(Consumer<BindingBlock> blockingRunnable, QualifiedName namespace, DataSource id) {
		blockAndWaitFor(block -> {
			blockingRunnable.accept(block);
			return null;
		} , namespace, id);
	}

	/**
	 * Create a block over the given {@link Consumer} blocking process, then wait
	 * until the resource is available.
	 * 
	 * @param blockingRunnable
	 *          The runnable which will block until its execution completes
	 * @param namespace
	 *          The namespace of the blocking resource
	 * @param id
	 *          The id of the blocking resource
	 * @return The new thread which is performing the blocking resource fetching
	 */
	default Thread blockFor(Consumer<BindingBlock> blockingRunnable, QualifiedName namespace, DataSource id) {
		Thread thread = new Thread(() -> {
			blockAndWaitFor(blockingRunnable, namespace, id);
		});
		thread.start();
		return thread;
	}

	/**
	 * Create an internal block over the given {@link Function} blocking process,
	 * then wait until the resource is available.
	 * 
	 * @param blockingSupplier
	 *          The runnable which will block until its execution completes
	 * @param namespace
	 *          The namespace of the blocking resource
	 * @param id
	 *          The id of the blocking resource
	 * @return The new thread which is performing the blocking resource fetching
	 */
	<T> T blockAndWaitForInternal(Function<BindingBlock, ? extends T> blockingSupplier, QualifiedName namespace, DataSource id);

	/**
	 * Create an internal block over the given {@link Consumer} blocking process,
	 * then wait until the resource is available.
	 * 
	 * @param blockingSupplier
	 *          The runnable which will block until its execution completes
	 * @param namespace
	 *          The namespace of the blocking resource
	 * @param id
	 *          The id of the blocking resource
	 * @return The new thread which is performing the blocking resource fetching
	 */
	default void blockAndWaitForInternal(Consumer<BindingBlock> blockingRunnable, QualifiedName namespace, DataSource id) {
		blockAndWaitForInternal(block -> {
			blockingRunnable.accept(block);
			return null;
		} , namespace, id);
	}

	/**
	 * Create an internal block over the given {@link Consumer} blocking process,
	 * then wait until the resource is available.
	 * 
	 * @param blockingRunnable
	 *          The runnable which will block until its execution completes
	 * @param namespace
	 *          The namespace of the blocking resource
	 * @param id
	 *          The id of the blocking resource
	 * @return The new thread which is performing the blocking resource fetching
	 */
	Thread blockForInternal(Consumer<BindingBlock> blockingRunnable, QualifiedName namespace, DataSource id);

	/**
	 * Register a thread which is participating in the binding/unbinding process.
	 * This helps the processor determine whether there is unblocked activity, or
	 * otherwise detect a deadlock or unsatisfied dependency.
	 * 
	 * @param processingThread
	 *          The thread participating in processing
	 */
	void addInternalProcessingThread(Thread processingThread);
}
