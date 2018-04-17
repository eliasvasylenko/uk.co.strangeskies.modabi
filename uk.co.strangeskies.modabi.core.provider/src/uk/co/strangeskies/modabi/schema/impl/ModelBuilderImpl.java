package uk.co.strangeskies.modabi.schema.impl;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

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

  private final List<Child<?>> children;
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
      List<Child<?>> children,
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
    return new ChildBuilderImpl<>(schemaBuilder, this::endChild);
  }

  SchemaBuilderImpl getSchemaBuilder() {
    return schemaBuilder;
  }

  ModelBuilderImpl endChild(ChildBuilderImpl<?> child) {
    List<Child<?>> overriddenChildren = (baseModel == null)
        ? emptyList()
        : baseModel.children().skip(children.size()).collect(toList());
    ChildImpl<?> newChild = new ChildImpl<>(child, children, overriddenChildren, dataType);

    List<Child<?>> newChildren = new ArrayList<>(newChild.index());
    newChildren.addAll(children);
    newChildren.addAll(overriddenChildren.subList(0, newChild.index() - newChildren.size()));
    newChildren.add(newChild);

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
