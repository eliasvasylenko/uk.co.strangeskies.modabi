package uk.co.strangeskies.modabi.schema.impl;

import java.util.stream.Stream;

import uk.co.strangeskies.modabi.schema.meta.AnonymousModelBuilder;
import uk.co.strangeskies.modabi.schema.meta.AnonymousModelBuilder.ChildrenStep;
import uk.co.strangeskies.modabi.schema.meta.ChildBuilder;

public class AnonymousModelBuilderImpl<E> implements AnonymousModelBuilder<E>, ChildrenStep<E> {
  private final AnonymousModelBuilderContext<E> context;
  private final ModelBuilderImpl modelBuilder;

  public AnonymousModelBuilderImpl(
      SchemaBuilderImpl schemaBuilder,
      AnonymousModelBuilderContext<E> context) {
    this.context = context;
    this.modelBuilder = new ModelBuilderImpl(schemaBuilder);
  }

  public AnonymousModelBuilderImpl(
      AnonymousModelBuilderContext<E> context,
      ModelBuilderImpl modelBuilder) {
    this.context = context;
    this.modelBuilder = modelBuilder;
  }

  @Override
  public ChildBuilder.PropertiesStep<ChildrenStep<E>> addChild() {
    return new ChildBuilderImpl<>(
        modelBuilder.getSchemaBuilder(),
        new ChildBuilderContext<ChildrenStep<E>>() {
          @Override
          public ChildrenStep<E> endChild(ChildBuilderImpl<?> child) {
            modelBuilder.endChild(child);
            return new AnonymousModelBuilderImpl<>(context, modelBuilder.endChild(child));
          }
        });
  }

  @Override
  public E endOverride() {
    return context.endOverride(this);
  }

  @Override
  public Stream<? extends ChildBuilder<?>> getChildren() {
    return modelBuilder.getChildren();
  }

  protected ModelBuilderImpl getModelBuilder() {
    return modelBuilder;
  }
}
