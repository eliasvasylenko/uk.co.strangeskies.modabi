package uk.co.strangeskies.modabi.impl.processing;

import static java.util.Collections.reverse;
import static java.util.stream.Collectors.toList;
import static uk.co.strangeskies.modabi.processing.ProcessingException.MESSAGES;
import static uk.co.strangeskies.reflection.token.TypedObject.typedObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import uk.co.strangeskies.modabi.ModabiException;
import uk.co.strangeskies.modabi.Provider;
import uk.co.strangeskies.modabi.processing.ProcessingContext;
import uk.co.strangeskies.modabi.processing.ProcessingException;
import uk.co.strangeskies.modabi.processing.Provisions;
import uk.co.strangeskies.reflection.token.TypeToken;
import uk.co.strangeskies.reflection.token.TypedObject;

public class ProvisionsImpl implements Provisions {
  private final ProcessingContext state;
  private final List<Provider> providers;

  public ProvisionsImpl(ProcessingContextImpl state) {
    this.state = state;

    List<Provider> contextProviders = new ArrayList<>(state.getProviders());
    reverse(contextProviders);
    List<Provider> managerProviders = state.manager().getProviders().collect(toList());

    this.providers = new ArrayList<>(contextProviders.size() + managerProviders.size());
    this.providers.addAll(contextProviders);
    this.providers.addAll(managerProviders);
  }

  @Override
  public <T> TypedObject<T> provide(TypeToken<T> type) {
    return typedObject(
        type,
        providers
            .stream()
            .map(p -> p.provide(type, state))
            .filter(Objects::nonNull)
            .findFirst()
            .<ModabiException>orElseThrow(
                () -> new ProcessingException(MESSAGES.noProviderFound(type), state)));
  }

  @Override
  public boolean isProvided(TypeToken<?> type) {
    return providers.stream().map(p -> p.provide(type, state)).anyMatch(Objects::nonNull);
  }
}
