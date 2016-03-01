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
import uk.co.strangeskies.modabi.io.BufferingDataTarget;
import uk.co.strangeskies.modabi.io.DataItem;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.io.Primitive;

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
public interface BindingBlocker {
	default <T> BindingBlock block(QualifiedName namespace, Primitive<T> idType, T id, boolean internal) {
		return block(namespace, DataItem.forDataOfType(idType, id), internal);
	}

	default BindingBlock block(QualifiedName namespace, DataItem<?> id, boolean internal) {
		return block(namespace, new BufferingDataTarget().put(id).buffer(), internal);
	}

	BindingBlock block(QualifiedName namespace, DataSource id, boolean internal);

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
