/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
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
package uk.co.strangeskies.modabi.io.structured;

import java.util.function.Function;

import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.io.DataTarget;

public interface StructuredDataTarget {
	public StructuredDataState currentState();

	/**
	 * This may help some data targets, e.g. XML, organise content a little more
	 * cleanly, by suggesting a default namespace for a document at the current
	 * child element (or the root element if none have been yet created).
	 * 
	 * The target should establish the namespace locally to the current child if
	 * possible, but may fall back to global scope if local scoping is not
	 * supported. The target may also fall back to equivalent behaviour to
	 * {@link StructuredDataTarget#registerNamespaceHint(Namespace)} if the
	 * concept of 'default' namespace doesn't apply.
	 * 
	 * If a DataTarget implementation does not support namespace hints this method
	 * should fail silently rather than throwing an exception.
	 *
	 * @param namespace
	 * @return
	 */
	public StructuredDataTarget registerDefaultNamespaceHint(Namespace namespace);

	public StructuredDataTarget registerNamespaceHint(Namespace namespace);

	public StructuredDataTarget nextChild(QualifiedName name);

	public DataTarget writeProperty(QualifiedName name);

	public default StructuredDataTarget writeProperty(QualifiedName name,
			Function<DataTarget, DataTarget> targetOperation) {
		DataTarget target = writeProperty(name);
		if (target != targetOperation.apply(target))
			throw new IllegalArgumentException();
		target.terminate();
		return this;
	}

	public DataTarget writeContent();

	public default StructuredDataTarget writeContent(
			Function<DataTarget, DataTarget> targetOperation) {
		DataTarget target = writeContent();
		if (target != targetOperation.apply(target))
			throw new IllegalArgumentException();
		target.terminate();
		return this;
	}

	public StructuredDataTarget endChild();

	public StructuredDataTarget comment(String comment);
}
