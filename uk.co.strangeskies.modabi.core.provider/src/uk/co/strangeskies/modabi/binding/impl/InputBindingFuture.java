/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
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
package uk.co.strangeskies.modabi.binding.impl;

import static java.util.function.Function.identity;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import uk.co.strangeskies.modabi.Binding;
import uk.co.strangeskies.modabi.binding.BindingFuture;
import uk.co.strangeskies.modabi.binding.Blocks;
import uk.co.strangeskies.modabi.binding.BindingException;
import uk.co.strangeskies.modabi.io.structured.StructuredDataReader;
import uk.co.strangeskies.modabi.schema.BindingPoint;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.observable.Observable;

public class InputBindingFuture<T> implements BindingFuture<T> {
  private final BindingContextImpl context;
  private final BindingPointFuture<? super T> bindingPoint;

  private final CompletableFuture<Model<? super T>> modelFuture;

  private final CompletableFuture<Binding<T>> dataFuture;

  private InputBindingFuture(
      BindingContextImpl context,
      BindingPointFuture<? super T> bindingPoint,
      CompletableFuture<? extends Binding<T>> dataFuture) {
    this.context = context;
    this.bindingPoint = bindingPoint;
    this.modelFuture = new CompletableFuture<>();
    this.dataFuture = dataFuture.handle((b, t) -> {
      if (b != null)
        return b;
      throw onFail(t);
    });
  }

  public static <T> InputBindingFuture<? extends T> readBindingFuture(
      BindingContextImpl context,
      BindingPointFuture<T> bindingPoint,
      StructuredDataFuture<StructuredDataReader> dataReader,
      ClassLoader classLoader) {
    CompletableFuture<Binding<? extends T>> dataFuture = CompletableFuture
        .allOf(bindingPoint, dataReader)
        .thenApply(
            v -> bind(context, classLoader, bindingPoint.getNow(null), dataReader.getNow(null)));

    return new InputBindingFuture<>(context, bindingPoint, dataFuture);
  }

  private static <T> Binding<? extends T> bind(
      BindingContextImpl context,
      ClassLoader classLoader,
      BindingPoint<T> bindingPoint,
      StructuredDataReader input) {
    context = context.withInput(input);

    return new NodeReader<T>().bind(context, bindingPoint);
  }
  /*
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * TODO Get rid of the manual thread counting rubbish as follows:
   * 
   * TODO We can know when 
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   */

  @Override
  public Binding<? extends T> get() throws InterruptedException, ExecutionException {
    return dataFuture.get();
  }

  @Override
  public Binding<? extends T> get(long timeout, TimeUnit unit)
      throws InterruptedException, ExecutionException {
    return dataFuture.get();
  }

  @Override
  public boolean cancel(boolean mayInterruptIfRunning) {
    return dataFuture.cancel(mayInterruptIfRunning);
  }

  @Override
  public boolean isCancelled() {
    return dataFuture.isCancelled();
  }

  @Override
  public boolean isDone() {
    return dataFuture.isDone();
  }

  private BindingException onFail(Throwable t) {
    context.cancel();

    String input = "unknown";

    String modelString = modelFuture.thenApply(m -> " with model '" + m.name() + "'").getNow("");
    return new BindingException(
        "Unexpected interrupt during binding of '" + input + "' with blocks '" + blocks() + "'"
            + modelString,
        context,
        t);
  }

  @Override
  public Future<Model<? super T>> getModelFuture() {
    return modelFuture;
  }

  @Override
  public Future<BindingPoint<? super T>> getBindingPointFuture() {
    return bindingPoint.thenApply(identity());
  }

  @Override
  public Blocks blocks() {
    // TODO implement Blocks interface
    return null;
  }

  @Override
  public Observable<Binding<T>> observable() {
    // TODO Auto-generated method stub
    return null;
  }
}
