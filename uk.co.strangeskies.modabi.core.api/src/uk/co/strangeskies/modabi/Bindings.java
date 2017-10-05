package uk.co.strangeskies.modabi;

import static uk.co.strangeskies.collection.stream.StreamUtilities.streamOptional;
import static uk.co.strangeskies.observable.Observer.onObservation;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import uk.co.strangeskies.collection.stream.StreamUtilities;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.observable.HotObservable;
import uk.co.strangeskies.observable.Observable;

public class Bindings {
  private final Set<Binding<?>> bindings;
  private final HotObservable<Binding<?>> bindingObservable;

  public Bindings() {
    bindings = new LinkedHashSet<>();
    bindingObservable = new HotObservable<>();
  }

  protected Object getMutex() {
    return bindings;
  }

  protected void add(Binding<?> binding) {
    synchronized (getMutex()) {
      if (!bindings.add(binding))
        throw new IllegalArgumentException();
      bindingObservable.next(binding);
    }
  }

  public Stream<Binding<?>> getAllBindings() {
    synchronized (getMutex()) {
      return bindings.stream();
    }
  }

  public Observable<Binding<?>> getAllFutureBindings() {
    synchronized (getMutex()) {
      return Observable.concat(
          Observable.of(bindings).then(onObservation(o -> o.requestUnbounded())),
          bindingObservable);
    }
  }

  @SuppressWarnings("unchecked")
  private static <T> Optional<Binding<? extends T>> matchModel(Binding<?> binding, Model<T> model) {
    if (StreamUtilities.<Model<?>>flatMapRecursive(model, m -> m.baseModels()).anyMatch(
        model::equals)) {
      return Optional.of((Binding<? extends T>) binding);
    } else {
      return Optional.empty();
    }
  }

  public <T> Stream<Binding<? extends T>> getAllBindings(Model<T> model) {
    synchronized (getMutex()) {
      return getAllBindings().flatMap(b -> streamOptional(matchModel(b, model)));
    }
  }

  public <T> Observable<Binding<? extends T>> getAllFutureBindings(Model<T> model) {
    synchronized (getMutex()) {
      return getAllFutureBindings().concatMap(b -> Observable.of(matchModel(b, model)));
    }
  }
}
