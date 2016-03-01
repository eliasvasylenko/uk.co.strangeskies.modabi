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

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.utilities.Observable;

public interface BindingBlocks extends Observable<BindingBlock> {
	Set<QualifiedName> getBlockingNamespaces();

	Set<BindingBlock> getBlocks(QualifiedName namespace);

	Set<BindingBlock> getBlocks();

	void waitFor(BindingBlock block) throws InterruptedException;

	void waitFor(BindingBlock block, long timeoutMilliseconds) throws InterruptedException;

	void waitForAll(QualifiedName namespace) throws InterruptedException;

	void waitForAll(QualifiedName namespace, long timeoutMilliseconds) throws InterruptedException;

	void waitForAll() throws InterruptedException;

	void waitForAll(long timeoutMilliseconds) throws InterruptedException;

	boolean isBlocked();
}
