package uk.co.strangeskies.modabi;

import java.util.HashMap;
import java.util.Map;

import uk.co.strangeskies.collection.observable.ObservableSet;
import uk.co.strangeskies.modabi.processing.BindingFuture;
import uk.co.strangeskies.modabi.schema.Model;

public class Bindings {
  private final Map<QualifiedName, ObservableSet<BindingFuture<?>>> bindingFutures;
  private final Map<QualifiedName, ObservableSet<Binding<?>>> bindings;

  public Bindings() {
    bindingFutures = new HashMap<>();
    bindings = new HashMap<>();
  }

  protected <T> BindingFuture<T> registerBindingImpl(Binding<T> binding) {
    BindingFuture<T> future = BindingFuture.forBinding(binding);
    bindingFutures.get(binding.getModel().name()).add(future);
    bindings.get(binding.getModel().name()).add(binding);
    return future;
  }

  <T> BindingFuture<T> addBindingFuture(BindingFuture<T> bindingFuture) {
    new Thread(() -> {
      try {
        Model<? super T> model = bindingFuture.getModelFuture().get();
        QualifiedName modelName = model.name();

        bindingFutures.get(modelName).add(bindingFuture);

        try {
          bindings.get(model.name()).add(bindingFuture.get());
        } catch (Exception e) {
          bindingFutures.get(modelName).remove(bindingFuture);
        }
      } catch (Exception e) {}
    }).start();

    return bindingFuture;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public <T> ObservableSet<BindingFuture<T>> getBindingFutures(Model<T> model) {
    synchronized (bindingFutures.get(model.name())) {
      return (ObservableSet) bindingFutures.get(model.name());
    }
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public <T> ObservableSet<Binding<T>> getBindings(Model<T> model) {
    synchronized (bindings.get(model.name())) {
      return (ObservableSet) bindings.get(model.name());
    }
  }

}
