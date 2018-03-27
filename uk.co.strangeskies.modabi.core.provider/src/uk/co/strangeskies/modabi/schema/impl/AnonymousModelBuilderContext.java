package uk.co.strangeskies.modabi.schema.impl;

public interface AnonymousModelBuilderContext<E> {
  E endOverride(AnonymousModelBuilderImpl<?> childrenBuilder);
}
