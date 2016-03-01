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

import java.util.function.Supplier;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.io.DataSource;

public interface BindingFutureBlocker {
	/**
	 * Blocks are intended to signal that a thread is waiting for resources to be
	 * made available by some external process.
	 * <p>
	 * This method will block completion of the {@link BindingFuture} until the
	 * given runnable process completes. The process will be completed on the
	 * invoking thread, so invocation returns only after the supplier completes
	 * execution and the block is lifted.
	 * <p>
	 * Blocks may be satisfied by external processes, so if a block is still in
	 * place when binding otherwise completes, with no internal processing threads
	 * remaining unblocked, then the binding will simply wait until the supplier
	 * delivers the dependency.
	 * <p>
	 * The thread which the given blocking process is invoked on will always
	 * terminate after invocation, and after releasing the block. This can be
	 * useful for synchronisation, as the thread which provided the dependency can
	 * be {@link Thread#join()}ed onto this thread before blocking itself on a
	 * difference resource, potentially causing an erroneous deadlock detection.
	 * 
	 * @param blockingSupplier
	 *          The runnable which will block until its execution completes
	 * @param namespace
	 *          The namespace of the blocking resource
	 * @param id
	 *          The id of the blocking resource
	 * @return The new thread which is performing the blocking resource fetching
	 */
	<T> T blockAndWaitFor(Supplier<? extends T> blockingSupplier, QualifiedName namespace, DataSource id);

	/**
	 * Blocks are intended to signal that a thread is waiting for resources to be
	 * made available by some external process.
	 * <p>
	 * This method will block completion of the {@link BindingFuture} until the
	 * given runnable process completes. The process will be completed on another
	 * thread, so invocation returns immediately.
	 * <p>
	 * Blocks may be satisfied by external processes, so if a block is still in
	 * place when binding otherwise completes, with no internal processing threads
	 * remaining unblocked, then the binding will simply wait until the supplier
	 * delivers the dependency.
	 * <p>
	 * The thread which the given blocking process is invoked on will always
	 * terminate after invocation, and after releasing the block. This can be
	 * useful for synchronisation, as the thread which provided the dependency can
	 * be {@link Thread#join()}ed onto this thread before blocking itself on a
	 * difference resource, potentially causing an erroneous deadlock detection.
	 * 
	 * @param blockingRunnable
	 *          The runnable which will block until its execution completes
	 * @param namespace
	 *          The namespace of the blocking resource
	 * @param id
	 *          The id of the blocking resource
	 * @return The new thread which is performing the blocking resource fetching
	 */
	default Thread blockFor(Runnable blockingRunnable, QualifiedName namespace, DataSource id) {
		Thread thread = new Thread(() -> {
			blockAndWaitFor(() -> {
				blockingRunnable.run();
				return null;
			} , namespace, id);
		});
		thread.start();
		return thread;
	}

	/**
	 * Internal blocks are intended to signal that a thread is waiting for
	 * resources to be made available by other threads participating in the
	 * binding/unbinding process.
	 * <p>
	 * This method will block completion of the {@link BindingFuture} until the
	 * given runnable process completes. The process will be completed on the
	 * invoking thread, so invocation returns only after the supplier completes
	 * execution and the block is lifted.
	 * <p>
	 * Internal blocks must be satisfied by internal processes, so if an internal
	 * block is still in place when binding otherwise completes, with no external
	 * blocks in place and no internal processing threads remaining unblocked,
	 * then the binding will fail due to unsatisfied dependency.
	 * <p>
	 * The thread which the given blocking process is invoked on will always
	 * terminate after invocation, and after releasing the block. This can be
	 * useful for synchronisation, as the thread which provided the dependency can
	 * be {@link Thread#join()}ed onto this thread before blocking itself on a
	 * difference resource, potentially causing an erroneous deadlock detection.
	 * 
	 * @param blockingSupplier
	 *          The runnable which will block until its execution completes
	 * @param namespace
	 *          The namespace of the blocking resource
	 * @param id
	 *          The id of the blocking resource
	 * @return The new thread which is performing the blocking resource fetching
	 */
	<T> T blockAndWaitForInternal(Supplier<? extends T> blockingSupplier, QualifiedName namespace, DataSource id);

	/**
	 * Internal blocks are intended to signal that a thread is waiting for
	 * resources to be made available by other threads participating in the
	 * binding/unbinding process.
	 * <p>
	 * This method will block completion of the {@link BindingFuture} until the
	 * given runnable process completes. The process will be completed on another
	 * thread, so invocation returns immediately.
	 * <p>
	 * Internal blocks must be satisfied by internal processes, so if an internal
	 * block is still in place when binding otherwise completes, with no external
	 * blocks in place and no internal processing threads remaining unblocked,
	 * then the binding will fail due to unsatisfied dependency.
	 * <p>
	 * The thread which the given blocking process is invoked on will always
	 * terminate after invocation, and after releasing the block. This can be
	 * useful for synchronisation, as the thread which provided the dependency can
	 * be {@link Thread#join()}ed onto this thread before blocking itself on a
	 * difference resource, potentially causing an erroneous deadlock detection.
	 * 
	 * @param blockingRunnable
	 *          The runnable which will block until its execution completes
	 * @param namespace
	 *          The namespace of the blocking resource
	 * @param id
	 *          The id of the blocking resource
	 * @return The new thread which is performing the blocking resource fetching
	 */
	Thread blockForInternal(Runnable blockingRunnable, QualifiedName namespace, DataSource id);

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
