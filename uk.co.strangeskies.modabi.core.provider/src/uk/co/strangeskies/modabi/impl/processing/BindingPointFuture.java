package uk.co.strangeskies.modabi.impl.processing;

import static java.util.stream.Collectors.toList;
import static uk.co.strangeskies.modabi.Models.getInputBindingPoint;
import static uk.co.strangeskies.modabi.Models.getOutputBindingPoint;
import static uk.co.strangeskies.modabi.processing.ProcessingException.MESSAGES;

import java.util.concurrent.CompletableFuture;

import uk.co.strangeskies.modabi.Models;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.processing.ProcessingException;
import uk.co.strangeskies.modabi.schema.BindingPoint;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.observable.Observable;
import uk.co.strangeskies.reflection.token.TypeToken;

public class BindingPointFuture<T> extends CompletableFuture<BindingPoint<T>> {
  protected BindingPointFuture(ProcessingContextImpl context, BindingPoint<T> bindingPoint) {
    if (!context.manager().registeredModels().contains(bindingPoint.model())) {
      throw new ProcessingException(
          MESSAGES.noModelFound(
              bindingPoint.model().name(),
              context.manager().registeredModels().getAll().collect(toList()),
              bindingPoint.model().dataType()),
          context);
    }

    complete(bindingPoint);
  }

  protected BindingPointFuture(CompletableFuture<BindingPoint<T>> future) {
    future.thenAccept(this::complete).exceptionally(e -> {
      completeExceptionally(e);
      return null;
    });
  }

  public static BindingPointFuture<?> bindingPointFuture(
      ProcessingContextImpl context,
      QualifiedName name) {
    return new BindingPointFuture<>(
        context.manager().registeredModels().getFuture(name).thenApply(Models::getBindingPoint));
  }

  public static <T> BindingPointFuture<T> bindingPointFuture(
      ProcessingContextImpl context,
      Model<T> model) {
    return bindingPointFuture(context, Models.getBindingPoint(model));
  }

  public static <T> BindingPointFuture<T> bindingPointFuture(
      ProcessingContextImpl context,
      BindingPoint<T> bindingPoint) {
    return new BindingPointFuture<>(context, bindingPoint);
  }

  public static <T> BindingPointFuture<? extends T> inputBindingPointFuture(
      ProcessingContextImpl context,
      QualifiedName name,
      TypeToken<T> type) {
    return new BindingPointFuture<>(
        context.manager().registeredModels().getFuture(name).thenApply(
            m -> (BindingPoint<? extends T>) getInputBindingPoint(m, type)));
  }

  public static <T> BindingPointFuture<? extends T> inputBindingPointFuture(
      ProcessingContextImpl context,
      TypeToken<T> type) {
    return new BindingPointFuture<>(
        context
            .manager()
            .registeredModels()
            .getAllFuture()
            .map(m -> (BindingPoint<? extends T>) getInputBindingPoint(m, type))
            .concatMap(Observable::of)
            .getNext());
  }

  public static <T> BindingPointFuture<? super T> outputBindingPointFuture(
      ProcessingContextImpl context,
      QualifiedName name,
      TypeToken<T> type) {
    return new BindingPointFuture<>(
        context.manager().registeredModels().getFuture(name).thenApply(
            m -> (BindingPoint<? super T>) getOutputBindingPoint(m, type)));
  }

  public static <T> BindingPointFuture<? super T> outputBindingPointFuture(
      ProcessingContextImpl context,
      TypeToken<T> type) {
    return new BindingPointFuture<>(
        context
            .manager()
            .registeredModels()
            .getAllFuture()
            .map(m -> (BindingPoint<? super T>) getOutputBindingPoint(m, type))
            .concatMap(Observable::of)
            .getNext());
  }
}
