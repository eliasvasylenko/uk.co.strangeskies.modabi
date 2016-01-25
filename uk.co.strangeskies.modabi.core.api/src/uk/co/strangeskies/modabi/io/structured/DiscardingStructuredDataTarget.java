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
package uk.co.strangeskies.modabi.io.structured;

import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.io.DataTarget;
import uk.co.strangeskies.modabi.io.DiscardingDataTarget;

public class DiscardingStructuredDataTarget
		extends StructuredDataTargetImpl<DiscardingStructuredDataTarget> {
	@Override
	protected void registerDefaultNamespaceHintImpl(Namespace namespace) {}

	@Override
	protected void registerNamespaceHintImpl(Namespace namespace) {}

	@Override
	protected void commentImpl(String comment) {}

	@Override
	protected void nextChildImpl(QualifiedName name) {}

	@Override
	protected DataTarget writePropertyImpl(QualifiedName name) {
		return new DiscardingDataTarget();
	}

	@Override
	protected DataTarget writeContentImpl() {
		return new DiscardingDataTarget();
	}

	@Override
	protected void endChildImpl() {}
}
