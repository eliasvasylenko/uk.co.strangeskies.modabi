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
package uk.co.strangeskies.modabi.impl.processing;

import static java.util.function.Function.identity;
import static uk.co.strangeskies.modabi.processing.ProcessingException.MESSAGES;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import uk.co.strangeskies.modabi.Binding;
import uk.co.strangeskies.modabi.io.structured.StructuredDataReader;
import uk.co.strangeskies.modabi.io.structured.StructuredDataWriter;
import uk.co.strangeskies.modabi.processing.BindingFuture;
import uk.co.strangeskies.modabi.processing.Blocks;
import uk.co.strangeskies.modabi.processing.ProcessingContext;
import uk.co.strangeskies.modabi.processing.ProcessingException;
import uk.co.strangeskies.modabi.schema.BindingPoint;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.observable.Observable;
import uk.co.strangeskies.reflection.classloading.ContextClassLoaderExecutor;

public class OutputBindingFuture<T> implements BindingFuture<T> {
  private final ProcessingContextImpl context;
  private final BindingPointFuture<? super T> bindingPoint;

  private final CompletableFuture<Model<? super T>> modelFuture;

  private final CompletableFuture<Binding<T>> dataFuture;

  private OutputBindingFuture(
      ProcessingContextImpl context,
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

  public static <T> OutputBindingFuture<? super T> writeBindingFuture(
      ProcessingContextImpl context,
      BindingPointFuture<T> bindingPoint,
      StructuredDataFuture<StructuredDataWriter> dataWriter,
      ClassLoader classLoader,
      Object data) {
    CompletableFuture<Binding<? extends T>> dataFuture = CompletableFuture
        .allOf(bindingPoint, dataWriter)
        .thenApply(
            v -> bind(context, classLoader, bindingPoint.getNow(null), dataWriter.getNow(null)));

    return new OutputBindingFuture<>(context, bindingPoint, dataFuture);
  }

  private static <T> Binding<? extends T> bind(
      ProcessingContextImpl context,
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
   * TODO Where a thread would currently be blocking for a resource, require that
   * instead the thread registers a callback for when the resource is available
   * and then ends. This way the code which releases the lock can trigger the
   * callback and it can be delegated to an executor or something.
   * 
   * TODO is this feasible? Some of the blocks may occur in strange places such as
   * during object construction. This would make it impossible. Investigate!
   * 
   * TODO uh maybe not, still have to count active threads to detect deadlock...
   * perhaps require than things be executed on a special executor...
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

  private ProcessingException onFail(Throwable t) {
    context.cancel();

    String input = "unknown";

    String modelString = modelFuture.thenApply(m -> " with model '" + m.name() + "'").getNow("");
    return new ProcessingException(
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

  @SuppressWarnings("unchecked")
  private <U extends T> void unbindImpl(
      ProcessingContext context,
      Model<? super U> model,
      StructuredDataWriter output) {
    output.registerDefaultNamespaceHint(model.name().getNamespace());

    try {
      context.output().get().addChild(model.name());

      ClassLoader classLoader = this.classLoader != null
          ? this.classLoader
          : Thread.currentThread().getContextClassLoader();

      new ContextClassLoaderExecutor(r -> r.run(), classLoader)
          .execute(() -> new BindingNodeUnbinder(context, model, (U) data).unbind());

      context.output().get().endChild();
    } catch (ProcessingException e) {
      throw e;
    } catch (Exception e) {
      throw new ProcessingException(MESSAGES.unexpectedProblemProcessing(data, model), context, e);
    }
  }
}
