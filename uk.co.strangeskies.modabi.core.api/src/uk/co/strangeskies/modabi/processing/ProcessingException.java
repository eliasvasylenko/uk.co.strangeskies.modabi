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
package uk.co.strangeskies.modabi.processing;

import static java.lang.System.lineSeparator;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.function.Function;

import uk.co.strangeskies.modabi.ModabiException;
import uk.co.strangeskies.text.properties.Localized;
import uk.co.strangeskies.text.properties.PropertyLoader;

public class ProcessingException extends ModabiException {
	private static final long serialVersionUID = 1L;

	private final ProcessingContext state;
	private final Localized<String> bindingObjects;
	private final Localized<String> bindingNodes;

	private final Collection<? extends Throwable> multiCause;

	private ProcessingException(Function<ProcessingProperties, Localized<String>> message, ProcessingContext state,
			Collection<? extends Throwable> cause, ProcessingProperties text) {
		super(message.apply(text), cause.iterator().next());

		multiCause = cause;
		this.state = state;

		bindingObjects = text.bindingObjects(state.getBindingObjectStack());
		bindingNodes = text.bindingNodes(state.getBindingNodeStack());
	}

	public ProcessingException(Function<ProcessingProperties, Localized<String>> message, ProcessingContext state,
			Collection<? extends Throwable> cause) {
		this(message, state, cause, PropertyLoader.getDefaultPropertyLoader().getProperties(ProcessingProperties.class));
	}

	public ProcessingException(Function<ProcessingProperties, Localized<String>> message, ProcessingContext state,
			Throwable cause) {
		this(message, state, Arrays.asList(cause));
	}

	public ProcessingException(Function<ProcessingProperties, Localized<String>> message, ProcessingContext state) {
		this(message, state, (Throwable) null);
	}

	/*
	 * TODO remove {
	 */
	public ProcessingException(String message, ProcessingContext state, Throwable cause) {
		this(t -> Localized.forStaticLocale(message, Locale.ENGLISH), state, cause);
	}

	public ProcessingException(String message, ProcessingContext state) {
		this(message, state, (Exception) null);
	}

	public ProcessingException(String message, ProcessingContext state, Collection<? extends Throwable> cause) {
		this(t -> Localized.forStaticLocale(message, Locale.ENGLISH), state, cause);
	}
	/*
	 * TODO } remove
	 */

	@Override
	public String getMessage() {
		return super.getMessage() + lineSeparator() + bindingObjects + lineSeparator() + bindingNodes;
	}

	@Override
	public String getLocalizedMessage() {
		return super.getLocalizedMessage() + lineSeparator() + bindingObjects + lineSeparator() + bindingNodes;
	}

	public Collection<? extends Throwable> getMultipleCauses() {
		return multiCause;
	}

	public ProcessingContext getState() {
		return state;
	}
}
