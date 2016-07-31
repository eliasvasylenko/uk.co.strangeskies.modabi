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
package uk.co.strangeskies.modabi.schema;

import uk.co.strangeskies.reflection.ExecutableMember;
import uk.co.strangeskies.reflection.FieldMember;

public interface BindingChildNode<T, S extends BindingChildNode<T, S>> extends BindingNode<T, S>, InputNode<S> {
	enum OutputMemberType {
		FIELD, METHOD, NONE, SELF
	}

	OutputMemberType outputMemberType();

	ExecutableMember<?, ?> outputMethod();

	FieldMember<?, ?> outputField();

	Boolean synchronous();

	Boolean uncheckedOutput();

	/**
	 * If this method returns true, the return value of any invocation of the
	 * inMethod will replace the build class of any
	 *
	 * @return
	 */
	Boolean iterableOutput();

	Boolean castOutput();

	Boolean extensible();

	Boolean nullIfOmitted();

	@Override
	BindingChildNodeConfigurator<?, S, T> configurator();
}
