package uk.co.strangeskies.modabi.impl.processing;

import static java.util.stream.Collectors.toList;
import static uk.co.strangeskies.collection.stream.StreamUtilities.tryOptional;
import static uk.co.strangeskies.modabi.Models.cast;
import static uk.co.strangeskies.modabi.processing.ProcessingException.MESSAGES;
import static uk.co.strangeskies.modabi.schema.BindingPoint.anonymous;

import java.util.concurrent.CompletableFuture;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.processing.ProcessingException;
import uk.co.strangeskies.modabi.schema.BindingPoint;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.observable.Observable;
import uk.co.strangeskies.reflection.token.TypeToken;

public class BindingPointFuture<T> extends CompletableFuture<BindingPoint<T>> {
  protected BindingPointFuture(ProcessingContextImpl context, BindingPoint<T> bindingPoint) {
    if (!context.manager().registeredSchemata().contains(bindingPoint.model().schema())) {
      throw new ProcessingException(
          MESSAGES.noModelFound(
              bindingPoint.model().name(),
              context.registeredModels().getAll().collect(toList()),
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
        context.registeredModels().getFuture(name).thenApply(BindingPoint::anonymous));
  }

  public static <T> BindingPointFuture<T> bindingPointFuture(
      ProcessingContextImpl context,
      QualifiedName name,
      TypeToken<T> type) {
    return new BindingPointFuture<>(
        context.registeredModels().getFuture(name).thenApply(m -> cast(m, type)).thenApply(
            BindingPoint::anonymous));
  }

  public static <T> BindingPointFuture<T> bindingPointFuture(
      ProcessingContextImpl context,
      TypeToken<T> type) {
    return new BindingPointFuture<>(
        context
            .registeredModels()
            .getAllFuture()
            .map(m -> tryOptional(() -> cast(m, type)))
            .flatMap(Observable::of)
            .map(BindingPoint::anonymous)
            .getNext());
  }

  public static <T> BindingPointFuture<T> bindingPointFuture(
      ProcessingContextImpl context,
      Model<T> model) {
    return bindingPointFuture(context, anonymous(model));
  }

  public static <T> BindingPointFuture<T> bindingPointFuture(
      ProcessingContextImpl context,
      BindingPoint<T> bindingPoint) {
    return new BindingPointFuture<>(context, bindingPoint);
  }
}
