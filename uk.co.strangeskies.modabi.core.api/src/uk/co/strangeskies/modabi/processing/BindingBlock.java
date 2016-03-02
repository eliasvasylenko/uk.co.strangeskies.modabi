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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.io.DataSource;

/**
 * A {@link BindingBlock} is intended to represent that a binding thread is
 * waiting for resources or dependencies to be made available by some external
 * or internal process.
 * <p>
 * Each conceptual block should belong to a unique resource, and be given a
 * unique {@link #namespace()} and {@link #id()}.
 * 
 * @author Elias N Vasylenko
 */
public interface BindingBlock {
	/**
	 * @return The namespace of the blocking resource
	 */
	QualifiedName namespace();

	/**
	 * @return The id of the blocking resource
	 */
	DataSource id();

	/**
	 * @return True if the resource or dependency represented by this block is
	 *         satisfied or failed, false otherwise
	 */
	boolean isComplete();

	/**
	 * @return If the block has failed, the exception cause
	 */
	Throwable getFailure();

	/**
	 * @return True if the resource or dependency should be satisfied by processes
	 *         internal to the binding procedure, false otherwise
	 */
	boolean isInternal();

	/**
	 * Signal that the resource or dependency represented by this block has become
	 * available
	 */
	void complete();

	/**
	 * Wait for availability of the resource or dependency.
	 * 
	 * @throws InterruptedException
	 *           If the waiting thread is interrupted
	 * @throws ExecutionException
	 *           If the block has been notified of failure via
	 *           {@link #fail(Throwable)}
	 */
	void waitUntilComplete() throws InterruptedException, ExecutionException;

	/**
	 * Wait for availability of the resource or dependency, or throw an exception
	 * if the block does not complete within the given time.
	 * 
	 * @param timeoutMilliseconds
	 *          The amount of time to wait for completion
	 * @throws InterruptedException
	 *           If the waiting thread is interrupted
	 * @throws TimeoutException
	 *           If the block is not satisfied within the given time
	 * @throws ExecutionException
	 *           If the block has been notified of failure via
	 *           {@link #fail(Throwable)}
	 */
	void waitUntilComplete(long timeoutMilliseconds) throws InterruptedException, TimeoutException, ExecutionException;

	/**
	 * Signal a failure in making the resource or dependency available. If
	 * successful then pending and subsequent invocations of
	 * {@link #waitUntilComplete()} will rethrow the given error. The failure will
	 * be ignored if the block has already completed successfully.
	 * 
	 * @param cause
	 *          An exception detailing the cause of the failure to secure the
	 *          dependency and lift the block
	 * @return True if the failure was successfully registered, false otherwise
	 */
	boolean fail(Throwable cause);
}
