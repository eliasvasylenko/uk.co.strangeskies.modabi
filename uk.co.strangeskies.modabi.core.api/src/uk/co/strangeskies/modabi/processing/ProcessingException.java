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

import java.util.Collection;

import uk.co.strangeskies.modabi.ModabiException;
import uk.co.strangeskies.text.properties.PropertyLoader;

public class ProcessingException extends ModabiException {
  private static final long serialVersionUID = 1L;

  public static final ProcessingExceptionMessages MESSAGES = PropertyLoader
      .getDefaultPropertyLoader()
      .getProperties(ProcessingExceptionMessages.class);

  private final ProcessingContext state;
  private final String bindingObjects;
  private final String bindingNodes;

  public ProcessingException(String message, ProcessingContext state, Throwable cause) {
    super(message, cause);

    this.state = state;

    bindingObjects = MESSAGES.bindingObjects(state.getBindingObjectStack());
    bindingNodes = MESSAGES.bindingNodes(state.getBindingNodeStack());
  }

  public ProcessingException(String message, ProcessingContext state) {
    this(message, state, (Exception) null);
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
    return super.getLocalizedMessage() + lineSeparator() + bindingObjects + lineSeparator()
        + bindingNodes;
  }

  public ProcessingContext getState() {
    return state;
  }

  /**
   * Merge the given exceptions into a single processing exception according to
   * the following rules.
   * 
   * <p>
   * If the given set contains only one processing exception, any other exceptions
   * will be added to it via {@link Throwable#addSuppressed(Throwable)} then it
   * will be returned.
   * 
   * <p>
   * Otherwise if the given set is empty, a new processing exception will be
   * created over the given state with a generic failure message and returned.
   * 
   * <p>
   * Otherwise if the given set contains no processing exceptions, one will be
   * created over the given state with a generic message, the last exception in
   * the given collection will be selected as its {@link Throwable#getCause()
   * cause}, the rest will be added to it via
   * {@link Throwable#addSuppressed(Throwable)}, then it will be returned.
   * 
   * <p>
   * Otherwise the given set contains multiple processing exceptions, in which
   * case the one which reached the farthest processing index will be selected as
   * the primary exception, or the last one encountered in the case of a tie. All
   * other exceptions in the given set will be added to it via
   * {@link Throwable#addSuppressed(Throwable)} then it will be returned.
   * 
   * @param state
   *          the state at which the exceptions are caught
   * @param exceptions
   *          a set of exceptions to be merged into a single exception
   */
  public static ProcessingException mergeExceptions(
      ProcessingContext state,
      Collection<? extends Exception> exceptions) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException();
  }
}
