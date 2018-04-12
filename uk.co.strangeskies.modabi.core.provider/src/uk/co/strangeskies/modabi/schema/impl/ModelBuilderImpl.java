package uk.co.strangeskies.modabi.schema.impl;

import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.schema.Child;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.Permission;
import uk.co.strangeskies.modabi.schema.meta.ChildBuilder;
import uk.co.strangeskies.modabi.schema.meta.ModelBuilder;
import uk.co.strangeskies.modabi.schema.meta.ModelBuilder.ChildrenStep;
import uk.co.strangeskies.modabi.schema.meta.ModelBuilder.NameStep;
import uk.co.strangeskies.modabi.schema.meta.ModelBuilder.PropertiesStep;
import uk.co.strangeskies.modabi.schema.meta.SchemaBuilder;
import uk.co.strangeskies.reflection.token.TypeToken;

public class ModelBuilderImpl implements ModelBuilder, NameStep, PropertiesStep, ChildrenStep {
  private final SchemaBuilderImpl schemaBuilder;
  private final QualifiedName name;
  private final boolean partial;
  private final Permission permission;

  private final Model<?> baseModel;
  private final TypeToken<?> dataType;

  private final List<ChildImpl<?>> children;
  private final List<ChildBuilderImpl<?>> childBuilders;

  public ModelBuilderImpl(SchemaBuilderImpl schemaBuilder) {
    this.schemaBuilder = schemaBuilder;
    name = null;
    partial = false;
    permission = null;
    baseModel = null;
    dataType = null;
    children = emptyList();
    childBuilders = emptyList();
  }

  public ModelBuilderImpl(
      SchemaBuilderImpl schemaBuilder,
      QualifiedName name,
      boolean partial,
      Permission permission,
      Model<?> baseModel,
      TypeToken<?> dataType,
      List<ChildImpl<?>> children,
      List<ChildBuilderImpl<?>> childBuilders) {
    this.schemaBuilder = schemaBuilder;
    this.name = name;
    this.partial = partial;
    this.permission = permission;
    this.baseModel = baseModel;
    this.dataType = dataType;
    this.children = children;
    this.childBuilders = childBuilders;
  }

  protected Namespace getDefaultNamespace() {
    return schemaBuilder.getName().map(QualifiedName::getNamespace).get();
  }

  @Override
  public Optional<QualifiedName> getName() {
    return Optional.ofNullable(name);
  }

  @Override
  public PropertiesStep name(String string) {
    return name(new QualifiedName(string, getDefaultNamespace()));
  }

  @Override
  public PropertiesStep name(QualifiedName name) {
    return new ModelBuilderImpl(
        schemaBuilder,
        name,
        partial,
        permission,
        baseModel,
        dataType,
        children,
        childBuilders);
  }

  @Override
  public PropertiesStep partial() {
    return new ModelBuilderImpl(
        schemaBuilder,
        name,
        true,
        permission,
        baseModel,
        dataType,
        children,
        childBuilders);
  }

  @Override
  public boolean isPartial() {
    return partial;
  }

  @Override
  public PropertiesStep permission(Permission permission) {
    return new ModelBuilderImpl(
        schemaBuilder,
        name,
        partial,
        permission,
        baseModel,
        dataType,
        children,
        childBuilders);
  }

  @Override
  public Optional<Permission> getPermission() {
    return Optional.ofNullable(permission);
  }

  @Override
  public ChildBuilder.PropertiesStep<ChildrenStep> addChild() {
    return new ChildBuilderImpl<>(schemaBuilder, getChildBuilderContext());
  }

  SchemaBuilderImpl getSchemaBuilder() {
    return schemaBuilder;
  }

  ChildBuilderContext<ChildrenStep> getChildBuilderContext() {
    return new ChildBuilderContext<ChildrenStep>() {
      @Override
      public ChildrenStep endChild(ChildBuilderImpl<?> child) {
        Child<?> overriddenChild = (baseModel == null || name == null)
            ? null
            : baseModel.child(name);

        /*
         * TODO end child and decide how override behavior works
         * 
         * 1) if this overrides an existing node, the input/output should already be
         * defined there. All we need to do is add bounds to the inference if those
         * expressions mentioned any inference variables.
         * 
         * 2) otherwise the node must be added to the end of the sequence, so we take
         * the after types from the last node in the parent and use those as our before
         * types. Simple!
         * 
         */

        List<ChildImpl<?>> newChildren = new ArrayList<>(children.size() + 1);
        newChildren.addAll(children);
        newChildren.add(new ChildImpl<>(child, overriddenChild));

        List<ChildBuilderImpl<?>> newChildBuilders = new ArrayList<>(childBuilders.size() + 1);
        newChildBuilders.addAll(childBuilders);
        newChildBuilders.add(child);

        return new ModelBuilderImpl(
            schemaBuilder,
            name,
            partial,
            permission,
            baseModel,
            dataType,
            newChildren,
            newChildBuilders);
      }
    };
  }

  @Override
  public PropertiesStep baseModel(String name) {
    return baseModel(new QualifiedName(name, getDefaultNamespace()));
  }

  @Override
  public PropertiesStep baseModel(QualifiedName baseModel) {
    return new ModelBuilderImpl(
        schemaBuilder,
        name,
        partial,
        permission,
        schemaBuilder.getModel(baseModel),
        dataType,
        children,
        childBuilders);
  }

  @Override
  public Optional<QualifiedName> getBaseModel() {
    return getBaseModelImpl().map(Model::name);
  }

  public Optional<Model<?>> getBaseModelImpl() {
    return Optional.ofNullable(baseModel);
  }

  @Override
  public PropertiesStep type(TypeToken<?> dataType) {
    return new ModelBuilderImpl(
        schemaBuilder,
        name,
        partial,
        permission,
        baseModel,
        dataType,
        children,
        childBuilders);
  }

  @Override
  public Optional<TypeToken<?>> getDataType() {
    return Optional.ofNullable(dataType);
  }

  @Override
  public SchemaBuilder endModel() {
    return schemaBuilder.endModel(this);
  }

  @Override
  public Stream<? extends ChildBuilder<?>> getChildren() {
    return childBuilders.stream();
  }

  public Stream<? extends Child<?>> getChildrenImpl() {
    return children.stream();
  }
}
