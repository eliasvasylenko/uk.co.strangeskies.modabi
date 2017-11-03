package uk.co.strangeskies.modabi.binding.impl;

import static java.util.Collections.reverse;
import static java.util.stream.Collectors.toList;
import static uk.co.strangeskies.modabi.binding.BindingException.MESSAGES;
import static uk.co.strangeskies.reflection.token.TypedObject.typedObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import uk.co.strangeskies.modabi.ModabiException;
import uk.co.strangeskies.modabi.Provider;
import uk.co.strangeskies.modabi.binding.BindingContext;
import uk.co.strangeskies.modabi.binding.BindingException;
import uk.co.strangeskies.modabi.binding.Provisions;
import uk.co.strangeskies.reflection.token.TypeToken;
import uk.co.strangeskies.reflection.token.TypedObject;

public class ProvisionsImpl implements Provisions {
  private final BindingContext state;
  private final List<Provider> providers;

  public ProvisionsImpl(BindingContextImpl state) {
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
                () -> new BindingException(MESSAGES.noProviderFound(type), state)));
  }

  @Override
  public boolean isProvided(TypeToken<?> type) {
    return providers.stream().map(p -> p.provide(type, state)).anyMatch(Objects::nonNull);
  }
}
