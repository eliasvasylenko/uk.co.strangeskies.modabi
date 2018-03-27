package uk.co.strangeskies.modabi.schema.impl;

import static uk.co.strangeskies.reflection.Methods.findMethod;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.meta.AnonymousModelBuilder;
import uk.co.strangeskies.modabi.schema.meta.AnonymousModelBuilder.ChildrenStep;
import uk.co.strangeskies.modabi.schema.meta.ChildBuilder;
import uk.co.strangeskies.modabi.schema.meta.ModelBuilder;

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
    ChildBuilderContext<ModelBuilder.ChildrenStep> componentContext = modelBuilder
        .getChildBuilderContext();

    return new ChildBuilderImpl<>(
        modelBuilder.getSchemaBuilder(),
        new ChildBuilderContext<ChildrenStep<E>>() {
          @Override
          public Optional<Namespace> defaultNamespace() {
            return componentContext.defaultNamespace();
          }

          @Override
          public ChildrenStep<E> endChild(ChildBuilderImpl<?> child) {
            return new AnonymousModelBuilderImpl<>(
                context,

                // TODO can do better than a cast

                (ModelBuilderImpl) componentContext.endChild(child));
          }
        });
  }

  public <U> OverrideBuilder<U> overrideModelChildren(
      Function<Model<?>, ? extends U> node,
      Function<AnonymousModelBuilder<?>, Optional<? extends U>> builder) {
    return new OverrideBuilder<>(
        () -> findMethod(Model.class, node::apply).getName(),
        modelBuilder.getBaseModelImpl().map(node),
        builder.apply(this));
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
