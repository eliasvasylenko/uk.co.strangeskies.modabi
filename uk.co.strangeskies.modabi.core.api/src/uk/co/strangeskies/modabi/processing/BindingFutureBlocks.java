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

import java.util.List;
import java.util.Set;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.utilities.Observable;
import uk.co.strangeskies.utilities.tuple.Pair;

/*
 * TODO binding future actually waits for blocks before it can complete, rather than assuming blocks must be released if it reaches completion...
 */
public interface BindingFutureBlocks extends Observable<Pair<QualifiedName, DataSource>> {
	Set<QualifiedName> waitingForNamespaces();

	List<DataSource> waitingForIds(QualifiedName namespace);

	void waitFor(QualifiedName namespace, DataSource id) throws InterruptedException;

	void waitFor(QualifiedName namespace, DataSource id, long timeoutMilliseconds) throws InterruptedException;

	void waitForAll(QualifiedName namespace) throws InterruptedException;

	void waitForAll(QualifiedName namespace, long timeoutMilliseconds) throws InterruptedException;

	void waitForAll() throws InterruptedException;

	void waitForAll(long timeoutMilliseconds) throws InterruptedException;

	boolean isBlocked();
}
