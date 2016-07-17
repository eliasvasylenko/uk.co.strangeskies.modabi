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

import java.util.Locale;
import java.util.function.Function;

import uk.co.strangeskies.text.properties.Localized;
import uk.co.strangeskies.text.properties.LocalizedRuntimeException;
import uk.co.strangeskies.text.properties.PropertyLoader;

public class ModabiException extends LocalizedRuntimeException {
	private static final long serialVersionUID = 1L;

	public ModabiException(Localized<String> message) {
		super(message);
	}

	public ModabiException(Localized<String> message, Throwable cause) {
		super(message, cause);
	}

	public ModabiException(Function<ModabiExceptionText, Localized<String>> message) {
		this(message.apply(PropertyLoader.getDefaultPropertyLoader().getProperties(ModabiExceptionText.class)));
	}

	public ModabiException(Function<ModabiExceptionText, Localized<String>> message, Throwable cause) {
		this(message.apply(PropertyLoader.getDefaultPropertyLoader().getProperties(ModabiExceptionText.class)), cause);
	}

	public ModabiException(String message, Throwable cause) {
		this(Localized.forStaticLocale(message, Locale.ENGLISH), cause);
	}

	public ModabiException(String message) {
		this(message, null);
	}
}
