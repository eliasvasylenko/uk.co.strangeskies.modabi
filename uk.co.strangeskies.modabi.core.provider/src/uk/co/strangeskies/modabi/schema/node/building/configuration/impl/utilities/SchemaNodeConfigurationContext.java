/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.core.provider.
 *
 * uk.co.strangeskies.modabi.core.provider is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.core.provider is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.core.provider.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi.schema.node.building.configuration.impl.utilities;

import java.util.List;

import uk.co.strangeskies.modabi.namespace.Namespace;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.node.ChildNode;
import uk.co.strangeskies.modabi.schema.node.SchemaNode;
import uk.co.strangeskies.modabi.schema.node.building.DataLoader;
import uk.co.strangeskies.reflection.BoundSet;
import uk.co.strangeskies.reflection.TypeToken;

public interface SchemaNodeConfigurationContext<T extends ChildNode<?, ?>> {
	DataLoader dataLoader();

	boolean isAbstract();

	boolean isInputExpected();

	boolean isInputDataOnly();

	boolean isConstructorExpected();

	boolean isStaticMethodExpected();

	Namespace namespace();

	BoundSet boundSet();

	TypeToken<?> inputTargetType(QualifiedName node);

	TypeToken<?> outputSourceType();

	void addChild(T result);

	<U extends T> List<U> overrideChild(QualifiedName id, TypeToken<U> nodeClass);

	List<? extends SchemaNode<?, ?>> overriddenNodes();
}
