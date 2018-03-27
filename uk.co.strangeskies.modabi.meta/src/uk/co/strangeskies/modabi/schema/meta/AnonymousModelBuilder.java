package uk.co.strangeskies.modabi.schema.meta;

import java.util.function.Function;
import java.util.stream.Stream;

public interface AnonymousModelBuilder<E> {
  interface ChildrenStep<E> extends AnonymousModelBuilder<E> {
    ChildBuilder.PropertiesStep<ChildrenStep<E>> addChild();

    default ChildrenStep<E> addChild(
        Function<ChildBuilder.PropertiesStep<ChildrenStep<E>>, ChildBuilder<ChildrenStep<E>>> configuration) {
      return configuration.apply(addChild()).endChild();
    }
  }

  Stream<? extends ChildBuilder<?>> getChildren();

  E endOverride();
}
