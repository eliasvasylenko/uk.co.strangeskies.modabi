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

import static uk.co.strangeskies.utilities.text.Localizer.getDefaultLocalizer;

import java.util.Locale;
import java.util.function.Function;

import uk.co.strangeskies.modabi.ModabiException;
import uk.co.strangeskies.utilities.text.LocalizedString;

public class ModabiIoException extends ModabiException {
	private static final long serialVersionUID = 1L;

	public ModabiIoException(LocalizedString message, Throwable cause) {
		super(message, cause);
	}

	public ModabiIoException(LocalizedString message) {
		super(message);
	}

	public ModabiIoException(Function<ModabiIoExceptionText, LocalizedString> message) {
		this(message.apply(getDefaultLocalizer().getLocalization(ModabiIoExceptionText.class)));
	}

	public ModabiIoException(Function<ModabiIoExceptionText, LocalizedString> message, Throwable cause) {
		this(message.apply(getDefaultLocalizer().getLocalization(ModabiIoExceptionText.class)), cause);
	}

	public ModabiIoException(String message) {
		this(message, null);
	}

	public ModabiIoException(String message, Throwable cause) {
		this(LocalizedString.forStaticLocale(message, Locale.ENGLISH), cause);
	}
}
