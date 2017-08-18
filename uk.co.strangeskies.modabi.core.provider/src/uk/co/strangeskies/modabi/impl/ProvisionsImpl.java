package uk.co.strangeskies.modabi.impl;

import static uk.co.strangeskies.modabi.processing.ProcessingException.MESSAGES;
import static uk.co.strangeskies.reflection.token.TypedObject.typedObject;

import java.util.Objects;

import uk.co.strangeskies.modabi.ModabiException;
import uk.co.strangeskies.modabi.Provisions;
import uk.co.strangeskies.modabi.processing.ProcessingContext;
import uk.co.strangeskies.modabi.processing.ProcessingException;
import uk.co.strangeskies.reflection.token.TypeToken;
import uk.co.strangeskies.reflection.token.TypedObject;

public class ProvisionsImpl implements Provisions {

  @Override
  public <T> TypedObject<T> provide(TypeToken<T> type) {
    return typedObject(
        type,
        visiblePriovidersStream()
            .map(p -> p.provide(type, state))
            .filter(Objects::nonNull)
            .findFirst()
            .<ModabiException>orElseThrow(
                () -> new ProcessingException(MESSAGES.noProviderFound(type), state)));
  }

  @Override
  public boolean isProvided(TypeToken<?> type) {
    return visiblePriovidersStream().map(p -> p.provide(type, state)).anyMatch(Objects::nonNull);
  }

}
