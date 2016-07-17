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
package uk.co.strangeskies.modabi.io;

import java.util.Locale;
import java.util.function.Function;

import uk.co.strangeskies.modabi.ModabiException;
import uk.co.strangeskies.text.properties.Localized;
import uk.co.strangeskies.text.properties.PropertyLoader;

public class ModabiIoException extends ModabiException {
	private static final long serialVersionUID = 1L;

	public ModabiIoException(Localized<String> message, Throwable cause) {
		super(message, cause);
	}

	public ModabiIoException(Localized<String> message) {
		super(message);
	}

	public ModabiIoException(Function<ModabiIoExceptionText, Localized<String>> message) {
		this(message.apply(PropertyLoader.getDefaultPropertyLoader().getProperties(ModabiIoExceptionText.class)));
	}

	public ModabiIoException(Function<ModabiIoExceptionText, Localized<String>> message, Throwable cause) {
		this(message.apply(PropertyLoader.getDefaultPropertyLoader().getProperties(ModabiIoExceptionText.class)), cause);
	}

	public ModabiIoException(String message) {
		this(message, null);
	}

	public ModabiIoException(String message, Throwable cause) {
		this(Localized.forStaticLocale(message, Locale.ENGLISH), cause);
	}
}
