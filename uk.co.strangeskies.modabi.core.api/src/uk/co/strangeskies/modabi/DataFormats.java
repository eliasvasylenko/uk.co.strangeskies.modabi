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
package uk.co.strangeskies.modabi;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import uk.co.strangeskies.modabi.io.structured.StructuredDataFormat;
import uk.co.strangeskies.utilities.Observable;
import uk.co.strangeskies.utilities.ObservableImpl;

public class DataFormats implements Observable<StructuredDataFormat> {
	private final ObservableImpl<StructuredDataFormat> dataInterfaceObservers = new ObservableImpl<>();
	private final Map<String, StructuredDataFormat> dataInterfaces = new HashMap<>();

	public synchronized void registerDataFormat(StructuredDataFormat loader) {
		dataInterfaces.put(loader.getFormatId(), loader);
		dataInterfaceObservers.fire(loader);
	}

	public synchronized void unregisterDataFormat(StructuredDataFormat loader) {
		dataInterfaces.remove(loader.getFormatId(), loader);
	}

	public synchronized Set<StructuredDataFormat> getRegistered() {
		return new HashSet<>(dataInterfaces.values());
	}

	public synchronized StructuredDataFormat getDataFormat(String id) {
		return dataInterfaces.get(id);
	}

	public synchronized boolean addObserver(Consumer<? super StructuredDataFormat> observer) {
		return dataInterfaceObservers.addWeakObserver(observer);
	}

	public synchronized boolean removeObserver(Consumer<? super StructuredDataFormat> observer) {
		return dataInterfaceObservers.addWeakObserver(observer);
	}
}
