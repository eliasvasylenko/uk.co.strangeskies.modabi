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

import uk.co.strangeskies.modabi.schema.InputNode.InputMemberType;

public interface InputNodeConfigurator<S extends InputNodeConfigurator<S, N>, N extends InputNode<N>>
		extends ChildNodeConfigurator<S, N> {
	S inputNone();

	S inputMethod(String methodName);

	S inputField(String fieldName);

	InputMemberType getInputMemberType();

	String getInputMember();

	default String getInputMethod() {
		return getInputMemberType() != InputMemberType.FIELD ? getInputMember() : null;
	}

	default String getInputField() {
		return getInputMemberType() == InputMemberType.FIELD ? getInputMember() : null;
	}

	S chainedInput(boolean chained);

	Boolean getChainedInput();

	S uncheckedInput(boolean unchecked);

	Boolean getUncheckedInput();

	S castInput(boolean cast);

	Boolean getCastInput();
}
