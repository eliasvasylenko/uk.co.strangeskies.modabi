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
package uk.co.strangeskies.modabi.processing;

import java.util.List;

import uk.co.strangeskies.modabi.Provisions;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.io.structured.StructuredDataSource;
import uk.co.strangeskies.modabi.schema.DataNode;
import uk.co.strangeskies.modabi.schema.DataType;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.reflection.TypedObject;
import uk.co.strangeskies.utilities.collection.computingmap.ComputingMap;

public interface BindingContext extends BindingState {
	List<TypedObject<?>> boundObjectStack();

	default TypedObject<?> boundObject() {
		return boundObject(0);
	}

	default TypedObject<?> boundObject(int parent) {
		return boundObjectStack().get(bindingNodeStack().size() - (1 + parent));
	}

	Provisions provisions();

	Model.Effective<?> getModel(QualifiedName nextElement);

	<T> ComputingMap<DataType<? extends T>, DataNode.Effective<? extends T>> getDataNodeOverrides(
			DataNode<T> node);

	StructuredDataSource input();
}
